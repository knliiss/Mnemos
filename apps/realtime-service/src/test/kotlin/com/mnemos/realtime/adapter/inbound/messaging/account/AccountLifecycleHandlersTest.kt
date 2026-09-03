package com.mnemos.realtime.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountStatusChangedV1
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.realtime.application.delivery.model.AccountStatusChangedPayload
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventService
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class AccountLifecycleHandlersTest {
    @Test
    fun `deleted account event is delivered once to every distinct affected user`() {
        val eventSink = RecordingRealtimeEventSink()
        val publisher =
            AccountRealtimeEventPublisher(
                PublishRealtimeEventService(
                    replayStore = eventSink,
                    fanout = eventSink,
                    telemetry = eventSink,
                    clock = Clock.fixed(Instant.parse("2026-09-03T00:20:01Z"), ZoneOffset.UTC),
                ),
            )
        val handler = AccountDeletedHandler(publisher)
        val eventId = UUID.fromString("019c1129-ef54-7000-8000-000000000421")
        val accountId = UUID.fromString("019c1129-ef54-7000-8000-000000000422")
        val firstUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000423")
        val secondUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000424")
        val occurredAt = Instant.parse("2026-09-03T00:20:00Z")

        handler.handle(
            ConsumedMessage(
                messageId = eventId,
                type = AccountEventType.ACCOUNT_DELETED,
                version = AccountEventContract.VERSION,
                producer = "account-service",
                aggregateType = AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
                aggregateId = accountId,
                occurredAt = occurredAt,
                metadata = EventMetadata(actorUserId = firstUserId),
                payload =
                    AccountStatusChangedV1(
                        status = "DELETED",
                        recipientUserIds = listOf(firstUserId, secondUserId, firstUserId),
                    ),
            ),
        )

        assertEquals(2, eventSink.persistedEvents.size)
        assertEquals(setOf(firstUserId, secondUserId), eventSink.persistedEvents.map { it.userId }.toSet())
        assertEquals(2, eventSink.broadcastEvents.size)

        eventSink.persistedEvents.forEach { published ->
            assertEquals(eventId, published.id)
            assertEquals(RealtimeEventType.ACCOUNT_DELETED, published.type)
            assertEquals(occurredAt, published.occurredAt)
            assertNull(published.operationId)

            val payload = published.payload as AccountStatusChangedPayload

            assertEquals(accountId, payload.accountId)
            assertEquals("DELETED", payload.status)
        }
    }
}
