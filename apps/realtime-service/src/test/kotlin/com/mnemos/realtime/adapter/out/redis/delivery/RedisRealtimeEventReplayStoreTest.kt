package com.mnemos.realtime.adapter.out.redis.delivery

import com.mnemos.realtime.adapter.redis.delivery.serialization.RedisRealtimeEventCodec
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class RedisRealtimeEventReplayStoreTest {
    private val redisTemplate = mock<StringRedisTemplate>()
    private val codec = mock<RedisRealtimeEventCodec>()
    private val properties = RealtimeDeliveryProperties()
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val store =
        RedisRealtimeEventReplayStore(
            redisTemplate = redisTemplate,
            codec = codec,
            properties = properties,
            clock = clock,
        )

    @Test
    fun `appends replay data through one atomic Redis script`() {
        val event = event(EVENT_ID, NOW)

        whenever(codec.encode(event)).thenReturn(SERIALIZED_EVENT)
        whenever(
            redisTemplate.execute(
                any<RedisScript<Long>>(),
                any<List<String>>(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(1L)

        store.append(event)

        verify(redisTemplate).execute(
            any<RedisScript<Long>>(),
            eq(listOf("realtime:sse:replay:$USER_ID")),
            eq(SERIALIZED_EVENT),
            eq("99"),
            eq("86400000"),
        )
    }

    @Test
    fun `returns retained events after the acknowledged id in delivery order`() {
        val listOperations = mock<ListOperations<String, String>>()
        val acknowledged = event(EVENT_ID, NOW.minusSeconds(3))
        val next = event(NEXT_EVENT_ID, NOW.minusSeconds(2))
        val expired = event(EXPIRED_EVENT_ID, NOW.minus(properties.replayTtl).minusSeconds(1))

        whenever(redisTemplate.opsForList()).thenReturn(listOperations)
        whenever(
            listOperations.range(
                "realtime:sse:replay:$USER_ID",
                0,
                properties.replayLimit - 1,
            ),
        ).thenReturn(listOf("next", "acknowledged", "expired"))
        whenever(codec.decode("next")).thenReturn(next)
        whenever(codec.decode("acknowledged")).thenReturn(acknowledged)
        whenever(codec.decode("expired")).thenReturn(expired)

        val result = store.findAfter(USER_ID, EVENT_ID)

        assertEquals(listOf(next), result)
    }

    private fun event(
        id: UUID,
        occurredAt: Instant,
    ): RealtimeEvent =
        RealtimeEvent(
            id = id,
            userId = USER_ID,
            type = RealtimeEventType.WALLET_TRANSACTION_COMPLETED,
            occurredAt = occurredAt,
            operationId = null,
            payload =
                WalletTransactionCompletedPayload(
                    requestId = UUID.randomUUID(),
                    transactionId = UUID.randomUUID(),
                    ownerType = "ACCOUNT",
                    ownerId = USER_ID,
                    currency = "COIN",
                    source = "BET_PLACE",
                    sourceReferenceId = UUID.randomUUID(),
                    amount = 500,
                ),
        )

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-15T10:00:00Z")
        val USER_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000401")
        val EVENT_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000402")
        val NEXT_EVENT_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000403")
        val EXPIRED_EVENT_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000404")
        const val SERIALIZED_EVENT = "serialized-event"
    }
}
