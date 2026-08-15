package com.mnemos.realtime.adapter.inbound.messaging.bet

import com.mnemos.bet.contracts.BetAggregateType
import com.mnemos.bet.contracts.BetEventContract
import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.BetOperationCompletedV1
import com.mnemos.bet.contracts.shared.BetOperationResourceType
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.realtime.application.delivery.model.BetOperationCompletedPayload
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventService
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class BetOperationCompletedHandlerTest {
    @Test
    fun `publishes the final bet operation result to the owning user`() {
        val eventSink = RecordingRealtimeEventSink()
        val handler =
            BetOperationCompletedHandler(
                PublishRealtimeEventService(
                    replayStore = eventSink,
                    fanout = eventSink,
                    telemetry = eventSink,
                    clock = Clock.fixed(Instant.parse("2026-08-15T10:03:01Z"), ZoneOffset.UTC),
                ),
            )
        val eventId = UUID.fromString("019c1129-ef54-7000-8000-000000000060")
        val operationId = UUID.fromString("019c1129-ef54-7000-8000-000000000061")
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000062")
        val resourceId = UUID.fromString("019c1129-ef54-7000-8000-000000000063")
        val occurredAt = Instant.parse("2026-08-15T10:03:00Z")
        val payload =
            BetOperationCompletedV1(
                operationId = operationId,
                userId = userId,
                resourceType = BetOperationResourceType.EVENT_BET,
                resourceId = resourceId,
                status = BetOperationStatus.REJECTED,
                amount = 1_000,
                walletTransactionId = null,
                failureReason = "BETTING_CLOSED",
                occurredAt = occurredAt,
            )

        handler.handle(
            ConsumedMessage(
                messageId = eventId,
                type = BetEventType.OPERATION_COMPLETED,
                version = BetEventContract.VERSION,
                producer = "bet-service",
                aggregateType = BetAggregateType.OPERATION,
                aggregateId = operationId,
                occurredAt = occurredAt,
                metadata = EventMetadata(actorUserId = userId),
                payload = payload,
            ),
        )

        val published = eventSink.persistedEvents.single()

        assertEquals(eventId, published.id)
        assertEquals(userId, published.userId)
        assertEquals(RealtimeEventType.BET_OPERATION_COMPLETED, published.type)
        assertEquals(operationId, published.operationId)
        val realtimePayload = published.payload as BetOperationCompletedPayload

        assertEquals("REJECTED", realtimePayload.status)
        assertEquals("BETTING_CLOSED", realtimePayload.failureReason)
        assertEquals(listOf(published), eventSink.broadcastEvents)
    }
}
