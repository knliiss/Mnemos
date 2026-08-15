package com.mnemos.realtime.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.PersonalizationPurchaseCompletedV1
import com.mnemos.personalization.contracts.shared.PersonalizationPurchaseStatus
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.Instant
import java.util.UUID

class PersonalizationPurchaseCompletedHandlerTest {
    private val publishRealtimeEventUseCase = mock<PublishRealtimeEventUseCase>()
    private val handler = PersonalizationPurchaseCompletedHandler(publishRealtimeEventUseCase)
    private val operationId = UUID.fromString("019c1182-0000-7000-8000-000000000001")
    private val personalizationId = UUID.fromString("019c1182-0000-7000-8000-000000000002")
    private val userId = UUID.fromString("019c1182-0000-7000-8000-000000000003")
    private val transactionId = UUID.fromString("019c1182-0000-7000-8000-000000000004")
    private val occurredAt = Instant.parse("2026-08-15T19:10:00Z")

    @Test
    fun `accepts consistent confirmed purchase event`() {
        assertDoesNotThrow {
            handler.definition.validate(
                envelope(
                    payload = confirmedPayload(),
                    envelopeOccurredAt = occurredAt,
                ),
            )
        }
    }

    @Test
    fun `rejects purchase event with different envelope time`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    payload = confirmedPayload(),
                    envelopeOccurredAt = occurredAt.plusSeconds(1),
                ),
            )
        }
    }

    @Test
    fun `rejects failed purchase carrying wallet transaction`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    payload =
                        PersonalizationPurchaseCompletedV1(
                            operationId = operationId,
                            personalizationId = personalizationId,
                            userId = userId,
                            status = PersonalizationPurchaseStatus.FAILED,
                            walletTransactionId = transactionId,
                            failureReason = "INSUFFICIENT_BALANCE",
                            occurredAt = occurredAt,
                        ),
                    envelopeOccurredAt = occurredAt,
                ),
            )
        }
    }

    private fun envelope(
        payload: PersonalizationPurchaseCompletedV1,
        envelopeOccurredAt: Instant,
    ): EventEnvelope<PersonalizationPurchaseCompletedV1> =
        EventEnvelope(
            eventId = UUID.fromString("019c1182-0000-7000-8000-000000000005"),
            eventType = PersonalizationEventType.PURCHASE_COMPLETED,
            eventVersion = PersonalizationEventContract.VERSION,
            producer = "personalization-service",
            aggregateType = PersonalizationAggregateType.PURCHASE_OPERATION,
            aggregateId = operationId,
            occurredAt = envelopeOccurredAt,
            metadata = EventMetadata(actorUserId = userId),
            payload = payload,
        )

    private fun confirmedPayload(): PersonalizationPurchaseCompletedV1 =
        PersonalizationPurchaseCompletedV1(
            operationId = operationId,
            personalizationId = personalizationId,
            userId = userId,
            status = PersonalizationPurchaseStatus.CONFIRMED,
            walletTransactionId = transactionId,
            failureReason = null,
            occurredAt = occurredAt,
        )
}
