package com.mnemos.realtime.adapter.inbound.messaging.moderation

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.moderation.contracts.ModerationEventContract
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.SanctionStateV1
import com.mnemos.moderation.contracts.shared.SanctionStatus
import com.mnemos.moderation.contracts.shared.SanctionType
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.SanctionChangedPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventService
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ModerationRealtimeHandlersTest {
    @Test
    fun `active ban is delivered to affected user`() {
        val eventSink = RecordingRealtimeEventSink()
        val handler = SanctionActivatedHandler(createPublisher(eventSink))
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000501")
        val sanctionId = UUID.fromString("019c1129-ef54-7000-8000-000000000502")
        val occurredAt = Instant.parse("2026-09-03T04:15:00Z")

        handler.handle(
            sanctionMessage(
                eventType = ModerationEventType.SANCTION_ACTIVATED,
                sanctionId = sanctionId,
                userId = userId,
                status = SanctionStatus.ACTIVE,
                occurredAt = occurredAt,
            ),
        )

        assertEquals(1, eventSink.persistedEvents.size)
        assertEquals(1, eventSink.broadcastEvents.size)

        val event = eventSink.persistedEvents.single()
        val payload = event.payload as SanctionChangedPayload

        assertEquals(userId, event.userId)
        assertEquals(RealtimeEventType.SANCTION_ACTIVATED, event.type)
        assertEquals(occurredAt, event.occurredAt)
        assertNull(event.operationId)
        assertEquals(sanctionId, payload.sanctionId)
        assertEquals(userId, payload.userId)
        assertEquals("BAN", payload.type)
        assertEquals("ACTIVE", payload.status)
    }

    @Test
    fun `revoked ban is delivered to affected user`() {
        val eventSink = RecordingRealtimeEventSink()
        val handler = SanctionDeactivatedHandler(createPublisher(eventSink))
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000503")
        val sanctionId = UUID.fromString("019c1129-ef54-7000-8000-000000000504")
        val occurredAt = Instant.parse("2026-09-03T04:16:00Z")

        handler.handle(
            sanctionMessage(
                eventType = ModerationEventType.SANCTION_DEACTIVATED,
                sanctionId = sanctionId,
                userId = userId,
                status = SanctionStatus.REVOKED,
                occurredAt = occurredAt,
            ),
        )

        val event = eventSink.persistedEvents.single()
        val payload = event.payload as SanctionChangedPayload

        assertEquals(userId, event.userId)
        assertEquals(RealtimeEventType.SANCTION_DEACTIVATED, event.type)
        assertEquals("REVOKED", payload.status)
    }

    private fun createPublisher(eventSink: RecordingRealtimeEventSink): ModerationRealtimeEventPublisher =
        ModerationRealtimeEventPublisher(
            PublishRealtimeEventService(
                replayStore = eventSink,
                fanout = eventSink,
                telemetry = eventSink,
                clock = Clock.fixed(Instant.parse("2026-09-03T04:20:00Z"), ZoneOffset.UTC),
            ),
        )

    private fun sanctionMessage(
        eventType: String,
        sanctionId: UUID,
        userId: UUID,
        status: SanctionStatus,
        occurredAt: Instant,
    ): ConsumedMessage<SanctionStateV1> =
        ConsumedMessage(
            messageId = UUID.randomUUID(),
            type = eventType,
            version = ModerationEventContract.VERSION,
            producer = "moderation-service",
            aggregateType = ModerationEventContract.AGGREGATE_TYPE,
            aggregateId = sanctionId,
            occurredAt = occurredAt,
            metadata = EventMetadata(actorUserId = null),
            payload =
                SanctionStateV1(
                    sanctionId = sanctionId,
                    userId = userId,
                    type = SanctionType.BAN,
                    status = status,
                    occurredAt = occurredAt,
                ),
        )
}
