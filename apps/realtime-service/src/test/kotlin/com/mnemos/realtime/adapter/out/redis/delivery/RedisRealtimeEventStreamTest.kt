package com.mnemos.realtime.adapter.out.redis.delivery

import com.mnemos.realtime.adapter.redis.delivery.serialization.RedisRealtimeEventCodec
import com.mnemos.realtime.application.delivery.model.AccountStatusChangedPayload
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.port.out.ConnectedRealtimeEventSink
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Instant
import java.util.UUID

class RedisRealtimeEventStreamTest {
    private val redisTemplate = mock<StringRedisTemplate>()
    private val codec = mock<RedisRealtimeEventCodec>()
    private val connectedEventSink = mock<ConnectedRealtimeEventSink>()
    private val stream =
        RedisRealtimeEventStream(
            redisTemplate = redisTemplate,
            codec = codec,
            properties = RealtimeDeliveryProperties(),
            connectedEventSink = connectedEventSink,
        )

    @Test
    fun `delivers to connected user before distributed Redis fanout`() {
        val event =
            RealtimeEvent(
                id = UUID.fromString("019c1129-ef54-7000-8000-000000000701"),
                userId = UUID.fromString("019c1129-ef54-7000-8000-000000000702"),
                type = RealtimeEventType.ACCOUNT_DEACTIVATED,
                occurredAt = Instant.parse("2026-09-04T22:30:00Z"),
                operationId = null,
                payload =
                    AccountStatusChangedPayload(
                        accountId = UUID.fromString("019c1129-ef54-7000-8000-000000000703"),
                        status = "DEACTIVATED",
                    ),
            )

        whenever(redisTemplate.opsForStream<String, String>())
            .thenThrow(IllegalStateException("Redis unavailable"))

        assertThrows(IllegalStateException::class.java) {
            stream.publish(event)
        }

        verify(connectedEventSink).send(event)
    }
}
