package com.mnemos.realtime.adapter.out.redis.broadcast

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.UUID

@Component
class RedisRealtimeBroadcastReplayStore(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
    private val properties: RealtimeDeliveryProperties,
) : RealtimeBroadcastReplayStore {
    override fun append(broadcast: RealtimeBroadcast) {
        removeExpired()

        val serialized = objectMapper.writeValueAsString(broadcast)

        redisTemplate
            .opsForZSet()
            .add(
                BROADCASTS_KEY,
                serialized,
                broadcast.sequence.toDouble(),
            )
        redisTemplate
            .opsForZSet()
            .add(
                EXPIRATIONS_KEY,
                serialized,
                broadcast.expiresAt.toEpochMilli().toDouble(),
            )

        redisTemplate.expire(BROADCASTS_KEY, properties.replayTtl)
        redisTemplate.expire(EXPIRATIONS_KEY, properties.replayTtl)
    }

    override fun findPending(userId: UUID): List<RealtimeBroadcast> {
        removeExpired()

        val acknowledged = readAcknowledgedSequences(userId)
        val serialized =
            redisTemplate
                .opsForZSet()
                .range(BROADCASTS_KEY, 0, -1)
                .orEmpty()

        return serialized
            .map { value -> objectMapper.readValue(value, RealtimeBroadcast::class.java) }
            .filterNot { broadcast -> acknowledged.contains(broadcast.sequence) }
            .sortedWith(
                compareBy<RealtimeBroadcast> { broadcast -> broadcast.occurredAt }
                    .thenBy { broadcast -> broadcast.sequence },
            )
    }

    override fun acknowledge(
        userId: UUID,
        sequence: Long,
    ): Boolean {
        if (sequence <= 0) {
            return false
        }

        removeExpired()

        val exists =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    BROADCASTS_KEY,
                    sequence.toDouble(),
                    sequence.toDouble(),
                ).orEmpty()
                .isNotEmpty()

        if (!exists) {
            return false
        }

        val key = acknowledgedKey(userId)

        redisTemplate
            .opsForSet()
            .add(key, sequence.toString())
        redisTemplate.expire(key, properties.replayTtl)

        return true
    }

    private fun removeExpired() {
        val expired =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    EXPIRATIONS_KEY,
                    0.0,
                    clock.millis().toDouble(),
                ).orEmpty()

        if (expired.isEmpty()) {
            return
        }

        val members = expired.toTypedArray()

        redisTemplate.opsForZSet().remove(BROADCASTS_KEY, *members)
        redisTemplate.opsForZSet().remove(EXPIRATIONS_KEY, *members)
    }

    private fun readAcknowledgedSequences(userId: UUID): Set<Long> =
        redisTemplate
            .opsForSet()
            .members(acknowledgedKey(userId))
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()

    private fun acknowledgedKey(userId: UUID): String = "$ACKNOWLEDGED_KEY_PREFIX$userId"

    private companion object {
        const val BROADCASTS_KEY = "realtime:sse:broadcasts:v1"
        const val EXPIRATIONS_KEY = "realtime:sse:broadcast-expirations:v1"
        const val ACKNOWLEDGED_KEY_PREFIX = "realtime:sse:broadcast-acknowledged:v1:"
    }
}
