package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Instant
import java.util.UUID

class SseConnectionRegistryTest {
    @Test
    fun `merges replay and live events without sending duplicates`() {
        val registry =
            SseConnectionRegistry(
                properties = RealtimeDeliveryProperties(),
                telemetry = RecordingRealtimeEventSink(),
            )
        val emitter = mock<SseEmitter>()
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000020")
        val replayedEvent = createEvent(userId, "019c1129-ef54-7000-8000-000000000021")
        val liveEvent = createEvent(userId, "019c1129-ef54-7000-8000-000000000022")
        val connectionId = registry.register(userId, emitter)

        registry.send(replayedEvent)
        registry.completeReplay(
            userId = userId,
            connectionId = connectionId,
            replayEvents = listOf(replayedEvent),
        )
        registry.send(replayedEvent)

        verify(emitter, times(1)).send(any<SseEmitter.SseEventBuilder>())

        registry.send(liveEvent)

        verify(emitter, times(2)).send(any<SseEmitter.SseEventBuilder>())
    }

    @Test
    fun `propagates a replay delivery failure to the connection owner`() {
        val registry =
            SseConnectionRegistry(
                properties = RealtimeDeliveryProperties(),
                telemetry = RecordingRealtimeEventSink(),
            )
        val emitter = mock<SseEmitter>()
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000023")
        val replayedEvent = createEvent(userId, "019c1129-ef54-7000-8000-000000000024")
        val connectionId = registry.register(userId, emitter)
        val failure = IllegalStateException("send failed")

        doThrow(failure)
            .whenever(emitter)
            .send(any<SseEmitter.SseEventBuilder>())

        assertThrows(IllegalStateException::class.java) {
            registry.completeReplay(
                userId = userId,
                connectionId = connectionId,
                replayEvents = listOf(replayedEvent),
            )
        }
    }

    @Test
    fun `rejects connections above per user limit`() {
        val registry =
            SseConnectionRegistry(
                properties =
                    RealtimeDeliveryProperties(
                        maxConnectionsPerUser = 1,
                        maxConnectionsTotal = 10,
                    ),
                telemetry = RecordingRealtimeEventSink(),
            )
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000025")

        registry.register(userId, mock())

        assertThrows(RealtimeConnectionLimitExceededException::class.java) {
            registry.register(userId, mock())
        }
    }

    @Test
    fun `rejects connections above instance limit`() {
        val registry =
            SseConnectionRegistry(
                properties =
                    RealtimeDeliveryProperties(
                        maxConnectionsPerUser = 1,
                        maxConnectionsTotal = 1,
                    ),
                telemetry = RecordingRealtimeEventSink(),
            )

        registry.register(
            userId = UUID.fromString("019c1129-ef54-7000-8000-000000000026"),
            emitter = mock(),
        )

        assertThrows(RealtimeConnectionLimitExceededException::class.java) {
            registry.register(
                userId = UUID.fromString("019c1129-ef54-7000-8000-000000000027"),
                emitter = mock(),
            )
        }
    }

    @Test
    fun `releases quota when connection is unregistered`() {
        val registry =
            SseConnectionRegistry(
                properties =
                    RealtimeDeliveryProperties(
                        maxConnectionsPerUser = 1,
                        maxConnectionsTotal = 1,
                    ),
                telemetry = RecordingRealtimeEventSink(),
            )
        val firstUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000028")
        val secondUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000029")
        val connectionId = registry.register(firstUserId, mock())

        registry.unregister(
            userId = firstUserId,
            connectionId = connectionId,
        )

        assertDoesNotThrow {
            registry.register(secondUserId, mock())
        }
    }

    private fun createEvent(
        userId: UUID,
        eventId: String,
    ): RealtimeEvent =
        RealtimeEvent(
            id = UUID.fromString(eventId),
            userId = userId,
            type = RealtimeEventType.WALLET_TRANSACTION_COMPLETED,
            occurredAt = Instant.parse("2026-08-15T10:00:00Z"),
            operationId = null,
            payload =
                WalletTransactionCompletedPayload(
                    requestId = UUID.randomUUID(),
                    transactionId = UUID.randomUUID(),
                    ownerType = "ACCOUNT",
                    ownerId = userId,
                    currency = "COIN",
                    source = "BET_PLACE",
                    sourceReferenceId = UUID.randomUUID(),
                    amount = 500,
                ),
        )
}
