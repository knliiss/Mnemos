package com.mnemos.realtime.adapter.out.redis.broadcast

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Component
class RedisRealtimeBroadcastReplayStore(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : RealtimeBroadcastReplayStore {
    override fun append(broadcast: RealtimeBroadcast) {
        val serialized = objectMapper.writeValueAsString(broadcast)
        redisTemplate.opsForZSet().add(BROADCASTS_KEY, serialized, broadcast.sequence.toDouble())
        redisTemplate.expire(BROADCASTS_KEY, BROADCASTS_TTL)
    }

    override fun findPending(userId: UUID): List<RealtimeBroadcast> {
        val cursor = readCursor(userId)
        val serialized =
            redisTemplate
                .opsForZSet()
                .rangeByScore(BROADCASTS_KEY, (cursor + 1).toDouble(), Double.POSITIVE_INFINITY)
                .orEmpty()
        val now = Instant.now()

        return serialized
            .map { value -> value to objectMapper.readValue(value, RealtimeBroadcast::class.java) }
            .filter { (_, broadcast) -> broadcast.expiresAt.isAfter(now) }
            .sortedBy { (_, broadcast) -> broadcast.sequence }
            .map { (_, broadcast) -> broadcast }
    }

    override fun acknowledge(
        userId: UUID,
        sequence: Long,
    ): Boolean {
        if (sequence <= 0) {
            return false
        }

        val exists =
            redisTemplate
                .opsForZSet()
                .rangeByScore(BROADCASTS_KEY, sequence.toDouble(), sequence.toDouble())
                .orEmpty()
                .isNotEmpty()
        if (!exists) {
            return false
        }

        val current = readCursor(userId)
        if (sequence > current) {
            redisTemplate.opsForHash<String, String>().put(CURSORS_KEY, userId.toString(), sequence.toString())
            redisTemplate.expire(CURSORS_KEY, CURSORS_TTL)
        }

        return true
    }

    private fun readCursor(userId: UUID): Long =
        redisTemplate
            .opsForHash<String, String>()
            .get(CURSORS_KEY, userId.toString())
            ?.toLongOrNull()
            ?: 0L

    private companion object {
        const val BROADCASTS_KEY = "realtime:sse:broadcasts:v1"
        const val CURSORS_KEY = "realtime:sse:broadcast-cursors:v1"
        val BROADCASTS_TTL: Duration = Duration.ofHours(2)
        val CURSORS_TTL: Duration = Duration.ofDays(7)
    }
}
