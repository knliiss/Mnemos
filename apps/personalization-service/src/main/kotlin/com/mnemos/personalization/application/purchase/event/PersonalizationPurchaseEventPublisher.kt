package com.mnemos.personalization.application.purchase.event

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.PersonalizationPurchaseCompletedV1
import com.mnemos.personalization.contracts.shared.PersonalizationPurchaseStatus
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class PersonalizationPurchaseEventPublisher(
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) {
    fun publish(
        operation: PurchaseOperation,
        status: PersonalizationPurchaseStatus,
        failureReason: String? = null,
    ) {
        val occurredAt = clock.instant()

        publisher.publish(
            event =
                IntegrationEvent(
                    type = PersonalizationEventType.PURCHASE_COMPLETED,
                    aggregateId = operation.id.id,
                    occurredAt = occurredAt,
                    payload =
                        PersonalizationPurchaseCompletedV1(
                            operationId = operation.id.id,
                            personalizationId = operation.personalizationId.id,
                            userId = operation.buyerUserId.id,
                            status = status,
                            walletTransactionId = operation.walletTransactionId,
                            failureReason = failureReason,
                            occurredAt = occurredAt,
                        ),
                ),
            metadata =
                EventMetadata(
                    actorUserId = operation.buyerUserId.id,
                ),
        )
    }
}
