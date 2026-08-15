package com.mnemos.realtime.application.delivery.usecase.publish

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventFanout
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventReplayStore
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PublishRealtimeEventServiceTest {
    @Test
    fun `persists an event before broadcasting the same event`() {
        val calls = mutableListOf<String>()
        val repository = RecordingRepository(calls)
        val broadcaster = RecordingBroadcaster(calls)
        val service =
            PublishRealtimeEventService(
                replayStore = repository,
                fanout = broadcaster,
                telemetry = RecordingRealtimeEventSink(),
                clock = Clock.fixed(Instant.parse("2026-08-15T10:00:01Z"), ZoneOffset.UTC),
            )
        val eventId = UUID.fromString("019c1129-ef54-7000-8000-000000000010")
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000011")
        val sourceReferenceId = UUID.fromString("019c1129-ef54-7000-8000-000000000012")
        val occurredAt = Instant.parse("2026-08-15T10:00:00Z")
        val payload =
            WalletTransactionCompletedPayload(
                requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000013"),
                transactionId = UUID.fromString("019c1129-ef54-7000-8000-000000000014"),
                ownerType = "ACCOUNT",
                ownerId = userId,
                currency = "COIN",
                source = "BET_PLACE",
                sourceReferenceId = sourceReferenceId,
                amount = 500,
            )

        val result =
            service.execute(
                PublishRealtimeEventCommand(
                    eventId = eventId,
                    userId = userId,
                    occurredAt = occurredAt,
                    operationId = null,
                    payload = payload,
                ),
            )

        val expected =
            RealtimeEvent(
                id = eventId,
                userId = userId,
                type = RealtimeEventType.WALLET_TRANSACTION_COMPLETED,
                occurredAt = occurredAt,
                operationId = null,
                payload = payload,
            )

        assertEquals(expected, result)
        assertEquals(listOf(expected), repository.events)
        assertEquals(listOf(expected), broadcaster.events)
        assertEquals(listOf("append", "fanout"), calls)
    }

    private class RecordingRepository(
        private val calls: MutableList<String>,
    ) : RealtimeEventReplayStore {
        val events = mutableListOf<RealtimeEvent>()

        override fun append(event: RealtimeEvent) {
            calls += "append"
            events += event
        }

        override fun findAfter(
            userId: UUID,
            lastEventId: UUID,
        ): List<RealtimeEvent> = emptyList()
    }

    private class RecordingBroadcaster(
        private val calls: MutableList<String>,
    ) : RealtimeEventFanout {
        val events = mutableListOf<RealtimeEvent>()

        override fun publish(event: RealtimeEvent) {
            calls += "fanout"
            events += event
        }
    }
}
