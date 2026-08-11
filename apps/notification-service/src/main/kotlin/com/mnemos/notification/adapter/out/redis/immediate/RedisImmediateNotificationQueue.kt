package com.mnemos.notification.adapter.out.redis.immediate

import com.mnemos.notification.adapter.out.redis.model.RedisNotificationBatch
import com.mnemos.notification.adapter.out.redis.model.RedisNotificationMapper.toDomain
import com.mnemos.notification.adapter.out.redis.model.RedisNotificationMapper.toRedis
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidate
import com.mnemos.notification.application.notification.immediate.model.ClaimedImmediateNotification
import com.mnemos.notification.application.notification.immediate.port.out.ImmediateNotificationQueue
import com.mnemos.notification.application.notification.model.NotificationBatch
import com.mnemos.notification.configuration.redis.ImmediateNotificationRedisProperties
import com.mnemos.notification.configuration.redis.NotificationDeliveryQueueRedisProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Repository
class RedisImmediateNotificationQueue(
    private val redisTemplate: StringRedisTemplate,
    private val jsonMapper: JsonMapper,
    private val properties: ImmediateNotificationRedisProperties,
    private val deliveryProperties: NotificationDeliveryQueueRedisProperties,
    @Qualifier("enqueueImmediateScript") private val enqueueScript: RedisScript<Long>,
    @Qualifier("claimJsonQueueScript") private val claimScript: RedisScript<Long>,
    @Qualifier("releaseJsonQueueScript") private val releaseScript: RedisScript<Long>,
    @Qualifier("completeProcessingScript") private val completeScript: RedisScript<Long>,
    @Qualifier("handoffImmediateScript") private val handoffScript: RedisScript<Long>,
) : ImmediateNotificationQueue {
    override fun enqueue(
        batch: NotificationBatch,
        availableAt: Instant,
    ): Boolean =
        redisTemplate.execute(
            enqueueScript,
            listOf(
                properties.acceptedKey(batch.sourceId),
                properties.batchKey(batch.sourceId),
                properties.readyKey,
            ),
            batch.sourceId.toString(),
            jsonMapper.writeValueAsString(batch.toRedis(jsonMapper)),
            availableAt.toEpochMilli().toString(),
            properties.acceptedTtl.toMillis().toString(),
            properties.batchTtl.toMillis().toString(),
        ) == 1L

    override fun claimDue(
        now: Instant,
        limit: Int,
    ): List<ClaimedImmediateNotification> {
        require(limit > 0)

        val sourceIds =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    properties.readyKey,
                    Double.NEGATIVE_INFINITY,
                    now.toEpochMilli().toDouble(),
                    0,
                    limit.toLong(),
                ).orEmpty()

        return sourceIds.mapNotNull { claim(UUID.fromString(it), now) }
    }

    override fun complete(notification: ClaimedImmediateNotification) {
        val sourceId = notification.batch.sourceId
        redisTemplate.execute(
            completeScript,
            listOf(properties.processingKey(sourceId), properties.processingDueKey),
            sourceId.toString(),
        )
    }

    override fun release(
        notification: ClaimedImmediateNotification,
        retryAt: Instant,
    ) {
        release(notification.batch.sourceId, retryAt)
    }

    override fun recoverExpired(
        now: Instant,
        limit: Int,
    ): Int {
        require(limit > 0)

        val sourceIds =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    properties.processingDueKey,
                    Double.NEGATIVE_INFINITY,
                    now.toEpochMilli().toDouble(),
                    0,
                    limit.toLong(),
                ).orEmpty()

        return sourceIds.count { release(UUID.fromString(it), now) }
    }

    override fun handoff(
        notification: ClaimedImmediateNotification,
        candidates: List<DeliveryCandidate>,
    ): Boolean {
        require(candidates.isNotEmpty())

        val sourceId = notification.batch.sourceId
        val sortedCandidates = candidates.sortedBy { it.id.id }
        val keys =
            buildList {
                add(properties.processingKey(sourceId))
                add(properties.processingDueKey)
                sortedCandidates.forEach { candidate ->
                    add(deliveryProperties.candidateKey(candidate.id))
                    add(deliveryProperties.laneKey(candidate.priority))
                }
            }

        val arguments =
            buildList {
                add(sourceId.toString())
                sortedCandidates.forEach { candidate ->
                    add(jsonMapper.writeValueAsString(candidate.toRedis(jsonMapper)))
                    add(candidate.readyAt.toEpochMilli().toString())
                    add(candidate.id.id.toString())
                    add(deliveryProperties.candidateTtl.toMillis().toString())
                }
            }

        return redisTemplate.execute(handoffScript, keys, *arguments.toTypedArray()) == 1L
    }

    private fun claim(
        sourceId: UUID,
        now: Instant,
    ): ClaimedImmediateNotification? {
        val processingKey = properties.processingKey(sourceId)
        val claimed =
            redisTemplate.execute(
                claimScript,
                listOf(
                    properties.readyKey,
                    properties.batchKey(sourceId),
                    processingKey,
                    properties.processingDueKey,
                ),
                sourceId.toString(),
                now.toEpochMilli().toString(),
                now.plus(properties.visibilityTimeout).toEpochMilli().toString(),
                properties.processingTtl.toMillis().toString(),
            ) == 1L

        if (!claimed) return null

        return try {
            val serialized = requireNotNull(redisTemplate.opsForValue().get(processingKey))
            ClaimedImmediateNotification(
                jsonMapper.readValue(serialized, RedisNotificationBatch::class.java).toDomain(jsonMapper),
            )
        } catch (exception: Exception) {
            release(sourceId, now)
            throw exception
        }
    }

    private fun release(
        sourceId: UUID,
        retryAt: Instant,
    ): Boolean =
        redisTemplate.execute(
            releaseScript,
            listOf(
                properties.readyKey,
                properties.batchKey(sourceId),
                properties.processingKey(sourceId),
                properties.processingDueKey,
            ),
            sourceId.toString(),
            retryAt.toEpochMilli().toString(),
            properties.batchTtl.toMillis().toString(),
        ) == 1L
}
