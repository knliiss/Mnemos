package com.mnemos.personalization.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.personalization.application.revenue.usecase.schedule.ScheduleCreatorRevenueUseCase
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.PersonalizationPurchaseCompletedV1
import com.mnemos.personalization.contracts.shared.PersonalizationPurchaseStatus
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.Instant
import java.util.UUID

class CreatorRevenuePurchaseCompletedHandlerTest {
    private val useCase = mock<ScheduleCreatorRevenueUseCase>()
    private val handler = CreatorRevenuePurchaseCompletedHandler(useCase)
    private val operationId = UUID.fromString("019c1181-0000-7000-8000-000000000001")
    private val personalizationId = UUID.fromString("019c1181-0000-7000-8000-000000000002")
    private val userId = UUID.fromString("019c1181-0000-7000-8000-000000000003")
    private val transactionId = UUID.fromString("019c1181-0000-7000-8000-000000000004")
    private val occurredAt = Instant.parse("2026-08-15T19:05:00Z")

    @Test
    fun `accepts confirmed purchase with wallet transaction`() {
        assertDoesNotThrow {
            handler.definition.validate(
                envelope(
                    payload =
                        payload(
                            status = PersonalizationPurchaseStatus.CONFIRMED,
                            walletTransactionId = transactionId,
                            failureReason = null,
                        ),
                ),
            )
        }
    }

    @Test
    fun `rejects confirmed purchase without wallet transaction`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    payload =
                        payload(
                            status = PersonalizationPurchaseStatus.CONFIRMED,
                            walletTransactionId = null,
                            failureReason = null,
                        ),
                ),
            )
        }
    }

    @Test
    fun `rejects failed purchase without failure reason`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    payload =
                        payload(
                            status = PersonalizationPurchaseStatus.FAILED,
                            walletTransactionId = null,
                            failureReason = null,
                        ),
                ),
            )
        }
    }

    @Test
    fun `rejects purchase event with different actor`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    payload =
                        payload(
                            status = PersonalizationPurchaseStatus.CONFIRMED,
                            walletTransactionId = transactionId,
                            failureReason = null,
                        ),
                    actorUserId = UUID.fromString("019c1181-0000-7000-8000-000000000005"),
                ),
            )
        }
    }

    private fun envelope(
        payload: PersonalizationPurchaseCompletedV1,
        actorUserId: UUID = userId,
    ): EventEnvelope<PersonalizationPurchaseCompletedV1> =
        EventEnvelope(
            eventId = UUID.fromString("019c1181-0000-7000-8000-000000000006"),
            eventType = PersonalizationEventType.PURCHASE_COMPLETED,
            eventVersion = PersonalizationEventContract.VERSION,
            producer = "personalization-service",
            aggregateType = PersonalizationAggregateType.PURCHASE_OPERATION,
            aggregateId = operationId,
            occurredAt = occurredAt,
            metadata = EventMetadata(actorUserId = actorUserId),
            payload = payload,
        )

    private fun payload(
        status: PersonalizationPurchaseStatus,
        walletTransactionId: UUID?,
        failureReason: String?,
    ): PersonalizationPurchaseCompletedV1 =
        PersonalizationPurchaseCompletedV1(
            operationId = operationId,
            personalizationId = personalizationId,
            userId = userId,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            occurredAt = occurredAt,
        )
}
