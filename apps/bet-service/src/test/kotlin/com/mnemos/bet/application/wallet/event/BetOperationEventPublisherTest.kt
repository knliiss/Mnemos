package com.mnemos.bet.application.wallet.event

import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.BetOperationCompletedV1
import com.mnemos.bet.contracts.shared.BetOperationResourceType
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletTransactionId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class BetOperationEventPublisherTest {
    @Test
    fun `publishes a completed stake operation with user metadata`() {
        val occurredAt = Instant.parse("2026-08-15T10:06:00Z")
        val eventBetId =
            EventBetId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000090"),
            )
        val userId =
            UserId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000091"),
            )
        val transactionId =
            WalletTransactionId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000092"),
            )
        val operation =
            WalletOperation.create(
                accountId = AccountId.generate(),
                initiatedByUserId = userId,
                amount = 2_000,
                kind = WalletOperationKind.STAKE,
                eventBetId = eventBetId,
                eventBetResolutionId = null,
                raceParticipantId = null,
                createdAt = occurredAt.minusSeconds(1),
                eligibleAt = occurredAt,
            )
        operation.complete(transactionId)
        val recordingPublisher = RecordingIntegrationEventPublisher()
        val publisher =
            BetOperationEventPublisher(
                publisher = recordingPublisher,
                clock = Clock.fixed(occurredAt, ZoneOffset.UTC),
            )

        publisher.publish(
            operation = operation,
            status = BetOperationStatus.CONFIRMED,
        )

        val published = recordingPublisher.events.single()
        val payload = published.event.payload as BetOperationCompletedV1

        assertEquals(BetEventType.OPERATION_COMPLETED, published.event.type)
        assertEquals(operation.id.id, published.event.aggregateId)
        assertEquals(userId.id, published.metadata.actorUserId)
        assertEquals(operation.id.id, payload.operationId)
        assertEquals(userId.id, payload.userId)
        assertEquals(BetOperationResourceType.EVENT_BET, payload.resourceType)
        assertEquals(eventBetId.id, payload.resourceId)
        assertEquals(BetOperationStatus.CONFIRMED, payload.status)
        assertEquals(2_000L, payload.amount)
        assertEquals(transactionId.id, payload.walletTransactionId)
        assertEquals(occurredAt, payload.occurredAt)
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val events = mutableListOf<PublishedEvent>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            events += PublishedEvent(event, metadata)

            return UUID.fromString("019c1129-ef54-7000-8000-000000000093")
        }
    }

    private data class PublishedEvent(
        val event: IntegrationEvent<*>,
        val metadata: EventMetadata,
    )
}
