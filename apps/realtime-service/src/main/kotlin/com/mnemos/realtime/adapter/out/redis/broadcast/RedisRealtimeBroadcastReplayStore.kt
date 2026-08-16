package com.mnemos.realtime.adapter.out.redis.broadcast

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.util.UUID

@Component
class RedisRealtimeBroadcastReplayStore(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
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

        redisTemplate.expire(BROADCASTS_KEY, BROADCASTS_TTL)
        redisTemplate.expire(EXPIRATIONS_KEY, BROADCASTS_TTL)
    }

    override fun findPending(userId: UUID): List<RealtimeBroadcast> {
        removeExpired()

        val cursor = readCursor(userId)
        val serialized =
            redisTemplate
                .opsForZSet()
                .rangeByScore(
                    BROADCASTS_KEY,
                    (cursor + 1).toDouble(),
                    Double.POSITIVE_INFINITY,
                ).orEmpty()

        return serialized
            .map { value -> objectMapper.readValue(value, RealtimeBroadcast::class.java) }
            .sortedBy(RealtimeBroadcast::sequence)
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

        val current = readCursor(userId)

        if (sequence > current) {
            redisTemplate
                .opsForValue()
                .set(
                    cursorKey(userId),
                    sequence.toString(),
                    CURSOR_TTL,
                )
        }

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

    private fun readCursor(userId: UUID): Long =
        redisTemplate
            .opsForValue()
            .get(cursorKey(userId))
            ?.toLongOrNull()
            ?: 0L

    private fun cursorKey(userId: UUID): String = "$CURSOR_KEY_PREFIX$userId"

    private companion object {
        const val BROADCASTS_KEY = "realtime:sse:broadcasts:v1"
        const val EXPIRATIONS_KEY = "realtime:sse:broadcast-expirations:v1"
        const val CURSOR_KEY_PREFIX = "realtime:sse:broadcast-cursor:v1:"

        val BROADCASTS_TTL: Duration = Duration.ofHours(2)
        val CURSOR_TTL: Duration = Duration.ofDays(7)
    }
}
