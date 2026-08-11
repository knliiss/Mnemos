package com.mnemos.notification.adapter.out.redis.aggregation

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.redis.model.RedisNotificationItem
import com.mnemos.notification.adapter.out.redis.model.RedisNotificationMapper.toDomain
import com.mnemos.notification.adapter.out.redis.model.RedisNotificationMapper.toRedis
import com.mnemos.notification.application.notification.aggregation.model.ClaimedNotificationAggregation
import com.mnemos.notification.application.notification.aggregation.port.out.NotificationAggregationStore
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidate
import com.mnemos.notification.application.notification.model.NotificationBatch
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.configuration.redis.NotificationAggregationRedisProperties
import com.mnemos.notification.configuration.redis.NotificationDeliveryQueueRedisProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Repository
class RedisNotificationAggregationStore(
    private val redisTemplate: StringRedisTemplate,
    private val jsonMapper: JsonMapper,
    private val properties: NotificationAggregationRedisProperties,
    private val deliveryProperties: NotificationDeliveryQueueRedisProperties,
    @Qualifier("addAggregationScript") private val addScript: RedisScript<Long>,
    @Qualifier("claimAggregationScript") private val claimScript: RedisScript<Long>,
    @Qualifier("releaseAggregationScript") private val releaseScript: RedisScript<Long>,
    @Qualifier("completeProcessingScript") private val completeScript: RedisScript<Long>,
    @Qualifier("handoffAggregationScript") private val handoffScript: RedisScript<Long>,
) : NotificationAggregationStore {
    override fun add(
        batch: NotificationBatch,
        flushAt: Instant,
    ): Boolean {
        val recipients = batch.recipientIds.sortedBy { it.id }
        val item =
            NotificationItem(
                sourceId = batch.sourceId,
                payload = batch.payload,
                occurredAt = batch.occurredAt,
                expiresAt = batch.expiresAt,
            ).toRedis(jsonMapper)

        val keys =
            buildList {
                add(properties.acceptedKey(batch.sourceId))
                add(properties.dueKey)
                recipients.forEach { add(properties.bucketKey(it)) }
            }

        val arguments =
            buildList {
                add(batch.sourceId.toString())
                add(jsonMapper.writeValueAsString(item))
                add(flushAt.toEpochMilli().toString())
                add(properties.acceptedTtl.toMillis().toString())
                add(properties.bucketTtl.toMillis().toString())
                recipients.forEach { add(it.id.toString()) }
            }

        return redisTemplate.execute(addScript, keys, *arguments.toTypedArray()) == 1L
    }

    override fun claimDue(
        now: Instant,
        limit: Int,
    ): List<ClaimedNotificationAggregation> {
        require(limit > 0)

        val userIds =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    properties.dueKey,
                    Double.NEGATIVE_INFINITY,
                    now.toEpochMilli().toDouble(),
                    0,
                    limit.toLong(),
                ).orEmpty()

        return userIds.mapNotNull { claim(UserId(UUID.fromString(it)), now) }
    }

    override fun complete(aggregation: ClaimedNotificationAggregation) {
        val processingKey = properties.processingKey(aggregation.userId, aggregation.claimId)
        redisTemplate.execute(
            completeScript,
            listOf(processingKey, properties.processingDueKey),
            processingKey,
        )
    }

    override fun release(
        aggregation: ClaimedNotificationAggregation,
        retryAt: Instant,
    ) {
        release(
            userId = aggregation.userId,
            processingKey = properties.processingKey(aggregation.userId, aggregation.claimId),
            retryAt = retryAt,
        )
    }

    override fun recoverExpired(
        now: Instant,
        limit: Int,
    ): Int {
        require(limit > 0)

        val processingKeys =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    properties.processingDueKey,
                    Double.NEGATIVE_INFINITY,
                    now.toEpochMilli().toDouble(),
                    0,
                    limit.toLong(),
                ).orEmpty()

        return processingKeys.count { processingKey ->
            release(
                userId = properties.processingUserId(processingKey),
                processingKey = processingKey,
                retryAt = now,
            )
        }
    }

    override fun handoff(
        aggregation: ClaimedNotificationAggregation,
        candidate: DeliveryCandidate,
    ): Boolean {
        val processingKey = properties.processingKey(aggregation.userId, aggregation.claimId)
        val serializedCandidate = jsonMapper.writeValueAsString(candidate.toRedis(jsonMapper))

        return redisTemplate.execute(
            handoffScript,
            listOf(
                processingKey,
                properties.processingDueKey,
                deliveryProperties.candidateKey(candidate.id),
                deliveryProperties.laneKey(candidate.priority),
            ),
            serializedCandidate,
            candidate.readyAt.toEpochMilli().toString(),
            candidate.id.id.toString(),
            deliveryProperties.candidateTtl.toMillis().toString(),
        ) == 1L
    }

    private fun claim(
        userId: UserId,
        now: Instant,
    ): ClaimedNotificationAggregation? {
        val claimId = UUID.randomUUID()
        val processingKey = properties.processingKey(userId, claimId)
        val visibilityDeadline = now.plus(properties.visibilityTimeout)

        val claimed =
            redisTemplate.execute(
                claimScript,
                listOf(
                    properties.dueKey,
                    properties.bucketKey(userId),
                    processingKey,
                    properties.processingDueKey,
                ),
                userId.id.toString(),
                now.toEpochMilli().toString(),
                visibilityDeadline.toEpochMilli().toString(),
                properties.processingTtl.toMillis().toString(),
            ) == 1L

        if (!claimed) return null

        return try {
            val items =
                redisTemplate
                    .opsForHash<String, String>()
                    .entries(processingKey)
                    .values
                    .map {
                        jsonMapper.readValue(it, RedisNotificationItem::class.java).toDomain(jsonMapper)
                    }.sortedWith(compareBy<NotificationItem>({ it.occurredAt }, { it.sourceId }))

            if (items.isEmpty()) {
                redisTemplate.execute(
                    completeScript,
                    listOf(processingKey, properties.processingDueKey),
                    processingKey,
                )
                null
            } else {
                ClaimedNotificationAggregation(
                    claimId = claimId,
                    userId = userId,
                    items = items,
                )
            }
        } catch (exception: Exception) {
            release(userId, processingKey, now)
            throw exception
        }
    }

    private fun release(
        userId: UserId,
        processingKey: String,
        retryAt: Instant,
    ): Boolean =
        redisTemplate.execute(
            releaseScript,
            listOf(
                properties.dueKey,
                processingKey,
                properties.bucketKey(userId),
                properties.processingDueKey,
            ),
            userId.id.toString(),
            retryAt.toEpochMilli().toString(),
            properties.bucketTtl.toMillis().toString(),
        ) == 1L
}
