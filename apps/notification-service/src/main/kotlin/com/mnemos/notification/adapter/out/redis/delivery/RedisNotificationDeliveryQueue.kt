package com.mnemos.notification.adapter.out.redis.delivery

import com.mnemos.notification.adapter.out.redis.model.RedisDeliveryCandidate
import com.mnemos.notification.adapter.out.redis.model.RedisNotificationMapper.toDomain
import com.mnemos.notification.application.notification.delivery.model.ClaimedDeliveryCandidate
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidateId
import com.mnemos.notification.application.notification.delivery.model.DeliveryPriority
import com.mnemos.notification.application.notification.delivery.port.out.NotificationDeliveryQueue
import com.mnemos.notification.configuration.redis.NotificationDeliveryQueueRedisProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Repository
class RedisNotificationDeliveryQueue(
    private val redisTemplate: StringRedisTemplate,
    private val jsonMapper: JsonMapper,
    private val properties: NotificationDeliveryQueueRedisProperties,
    @Qualifier("claimJsonQueueScript") private val claimScript: RedisScript<Long>,
    @Qualifier("releaseJsonQueueScript") private val releaseScript: RedisScript<Long>,
    @Qualifier("completeProcessingScript") private val completeScript: RedisScript<Long>,
) : NotificationDeliveryQueue {
    override fun claim(
        priority: DeliveryPriority,
        now: Instant,
    ): ClaimedDeliveryCandidate? {
        val candidateId =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    properties.laneKey(priority),
                    Double.NEGATIVE_INFINITY,
                    now.toEpochMilli().toDouble(),
                    0,
                    1,
                ).orEmpty()
                .firstOrNull()
                ?.let(UUID::fromString)
                ?: return null

        val id = DeliveryCandidateId(candidateId)
        val processingKey = properties.processingKey(id)
        val claimed =
            redisTemplate.execute(
                claimScript,
                listOf(
                    properties.laneKey(priority),
                    properties.candidateKey(id),
                    processingKey,
                    properties.processingDueKey,
                ),
                id.id.toString(),
                now.toEpochMilli().toString(),
                now.plus(properties.visibilityTimeout).toEpochMilli().toString(),
                properties.processingTtl.toMillis().toString(),
            ) == 1L

        if (!claimed) return null

        return try {
            val serialized = requireNotNull(redisTemplate.opsForValue().get(processingKey))
            ClaimedDeliveryCandidate(
                jsonMapper.readValue(serialized, RedisDeliveryCandidate::class.java).toDomain(jsonMapper),
            )
        } catch (exception: Exception) {
            redisTemplate.execute(
                completeScript,
                listOf(processingKey, properties.processingDueKey),
                id.id.toString(),
            )
            throw exception
        }
    }

    override fun complete(candidate: ClaimedDeliveryCandidate) {
        val id = candidate.candidate.id
        redisTemplate.execute(
            completeScript,
            listOf(properties.processingKey(id), properties.processingDueKey),
            id.id.toString(),
        )
    }

    override fun release(
        candidate: ClaimedDeliveryCandidate,
        retryAt: Instant,
    ) {
        release(candidate.candidate.id, candidate.candidate.priority, retryAt)
    }

    override fun recoverExpired(
        now: Instant,
        limit: Int,
    ): Int {
        require(limit > 0)

        val candidateIds =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    properties.processingDueKey,
                    Double.NEGATIVE_INFINITY,
                    now.toEpochMilli().toDouble(),
                    0,
                    limit.toLong(),
                ).orEmpty()

        return candidateIds.count { rawId ->
            val id = DeliveryCandidateId(UUID.fromString(rawId))
            val serialized = redisTemplate.opsForValue().get(properties.processingKey(id))
            if (serialized == null) {
                redisTemplate.opsForZSet().remove(properties.processingDueKey, rawId)
                false
            } else {
                val candidate = jsonMapper.readValue(serialized, RedisDeliveryCandidate::class.java).toDomain(jsonMapper)
                release(id, candidate.priority, now)
            }
        }
    }

    private fun release(
        id: DeliveryCandidateId,
        priority: DeliveryPriority,
        retryAt: Instant,
    ): Boolean =
        redisTemplate.execute(
            releaseScript,
            listOf(
                properties.laneKey(priority),
                properties.candidateKey(id),
                properties.processingKey(id),
                properties.processingDueKey,
            ),
            id.id.toString(),
            retryAt.toEpochMilli().toString(),
            properties.candidateTtl.toMillis().toString(),
        ) == 1L
}
