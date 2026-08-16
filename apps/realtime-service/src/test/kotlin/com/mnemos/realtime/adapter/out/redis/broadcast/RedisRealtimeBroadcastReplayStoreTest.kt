package com.mnemos.realtime.adapter.out.redis.broadcast

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.realtime.application.broadcast.model.DropSpotlightBroadcastPayload
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class RedisRealtimeBroadcastReplayStoreTest {
    @Test
    fun `acknowledging newer broadcast does not hide older pending broadcast`() {
        val now = Instant.parse("2026-08-16T00:30:00Z")
        val userId = UUID.fromString("019c1ab0-8800-7000-8000-000000000001")
        val older = broadcast(sequence = 9)
        val newer = broadcast(sequence = 10)
        val objectMapper = ObjectMapper().findAndRegisterModules()
        val redisTemplate = mock<StringRedisTemplate>()
        val zSetOperations = mock<ZSetOperations<String, String>>()
        val setOperations = mock<SetOperations<String, String>>()
        val store =
            RedisRealtimeBroadcastReplayStore(
                redisTemplate = redisTemplate,
                objectMapper = objectMapper,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        whenever(redisTemplate.opsForZSet()).thenReturn(zSetOperations)
        whenever(redisTemplate.opsForSet()).thenReturn(setOperations)
        whenever(
            zSetOperations.rangeByScore(
                "realtime:sse:broadcast-expirations:v1",
                0.0,
                now.toEpochMilli().toDouble(),
            ),
        ).thenReturn(emptySet())
        whenever(zSetOperations.range("realtime:sse:broadcasts:v1", 0, -1))
            .thenReturn(
                setOf(
                    objectMapper.writeValueAsString(older),
                    objectMapper.writeValueAsString(newer),
                ),
            )
        whenever(setOperations.members("realtime:sse:broadcast-acknowledged:v1:$userId"))
            .thenReturn(setOf(newer.sequence.toString()))

        val pending = store.findPending(userId)

        assertEquals(listOf(older.sequence), pending.map(RealtimeBroadcast::sequence))
    }

    private fun broadcast(sequence: Long): RealtimeBroadcast {
        val publishedAt = Instant.parse("2026-08-16T00:00:00Z").plusSeconds(sequence)
        val expiresAt = publishedAt.plusSeconds(3600)
        val spotlightId = UUID.fromString("019c1ab0-8800-7000-8000-%012d".format(sequence))

        return RealtimeBroadcast(
            id = spotlightId,
            sequence = sequence,
            occurredAt = publishedAt,
            expiresAt = expiresAt,
            payload =
                DropSpotlightBroadcastPayload(
                    spotlightId = spotlightId,
                    sequence = sequence,
                    sourceAccountId = UUID.fromString("019c1ab0-8800-7000-8000-000000000101"),
                    itemDropEventId = UUID.fromString("019c1ab0-8800-7000-8000-000000000102"),
                    droppedFor = "Knalis",
                    itemId = UUID.fromString("019c1ab0-8800-7000-8000-000000000103"),
                    itemName = "Elucidator",
                    itemRarity = "MYTHICAL",
                    itemType = "SWORD",
                    notificationPack = null,
                    publishedAt = publishedAt,
                    expiresAt = expiresAt,
                    minimumPresentationGapSeconds = 30,
                ),
        )
    }
}
