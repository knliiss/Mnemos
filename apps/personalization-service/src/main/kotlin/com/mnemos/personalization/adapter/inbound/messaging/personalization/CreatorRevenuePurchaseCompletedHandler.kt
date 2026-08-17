package com.mnemos.personalization.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.application.revenue.usecase.schedule.ScheduleCreatorRevenueUseCase
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.PersonalizationPurchaseCompletedV1
import com.mnemos.personalization.contracts.shared.PersonalizationPurchaseStatus
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import org.springframework.stereotype.Component

@Component
class CreatorRevenuePurchaseCompletedHandler(
    private val scheduleCreatorRevenueUseCase: ScheduleCreatorRevenueUseCase,
) : KafkaMessageHandler<PersonalizationPurchaseCompletedV1> {
    override val definition: KafkaMessageDefinition<PersonalizationPurchaseCompletedV1> =
        kafkaMessageDefinition(
            topic = PersonalizationEventContract.TOPIC,
            type = PersonalizationEventType.PURCHASE_COMPLETED,
            version = PersonalizationEventContract.VERSION,
            aggregateType = PersonalizationAggregateType.PURCHASE_OPERATION,
        ) { envelope ->
            if (envelope.producer != PERSONALIZATION_SERVICE) {
                throw KafkaMessageProtocolException(
                    "Personalization purchase results must be produced by personalization-service",
                )
            }

            if (envelope.aggregateId != envelope.payload.operationId) {
                throw KafkaMessageProtocolException(
                    "Personalization purchase aggregateId must match operationId",
                )
            }

            if (envelope.metadata.actorUserId != envelope.payload.userId) {
                throw KafkaMessageProtocolException(
                    "Personalization purchase actor must match userId",
                )
            }

            if (envelope.occurredAt != envelope.payload.occurredAt) {
                throw KafkaMessageProtocolException(
                    "Personalization purchase occurredAt must match envelope occurredAt",
                )
            }

            validateResultState(envelope.payload)
        }

    override val consumer = "personalization-service.creator-revenue-purchase-completed"

    override fun handle(message: ConsumedMessage<PersonalizationPurchaseCompletedV1>) {
        if (message.payload.status != PersonalizationPurchaseStatus.CONFIRMED) {
            return
        }

        scheduleCreatorRevenueUseCase.execute(PurchaseOperationId(message.payload.operationId))
    }

    private fun validateResultState(payload: PersonalizationPurchaseCompletedV1) {
        val valid =
            when (payload.status) {
                PersonalizationPurchaseStatus.CONFIRMED ->
                    payload.walletTransactionId != null && payload.failureReason == null

                PersonalizationPurchaseStatus.FAILED ->
                    payload.walletTransactionId == null && !payload.failureReason.isNullOrBlank()
            }

        if (!valid) {
            throw KafkaMessageProtocolException(
                "Personalization purchase result fields do not match status",
            )
        }
    }

    private companion object {
        const val PERSONALIZATION_SERVICE = "personalization-service"
    }
}
