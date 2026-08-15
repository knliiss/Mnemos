package com.mnemos.realtime.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.PersonalizationPurchaseCompletedV1
import com.mnemos.realtime.application.delivery.model.PersonalizationPurchaseCompletedPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventCommand
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import org.springframework.stereotype.Component

@Component
class PersonalizationPurchaseCompletedHandler(
    private val publishRealtimeEventUseCase: PublishRealtimeEventUseCase,
) : KafkaMessageHandler<PersonalizationPurchaseCompletedV1> {
    override val definition =
        kafkaMessageDefinition<PersonalizationPurchaseCompletedV1>(
            PersonalizationEventContract.TOPIC,
            PersonalizationEventType.PURCHASE_COMPLETED,
            PersonalizationEventContract.VERSION,
            PersonalizationAggregateType.PURCHASE_OPERATION,
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
        }

    override val consumer = "realtime-service.personalization-purchase-completed"

    override fun handle(message: ConsumedMessage<PersonalizationPurchaseCompletedV1>) {
        val event = message.payload

        publishRealtimeEventUseCase.execute(
            PublishRealtimeEventCommand(
                eventId = message.messageId,
                userId = event.userId,
                occurredAt = event.occurredAt,
                operationId = event.operationId,
                payload =
                    PersonalizationPurchaseCompletedPayload(
                        personalizationId = event.personalizationId,
                        status = event.status.name,
                        walletTransactionId = event.walletTransactionId,
                        failureReason = event.failureReason,
                    ),
            ),
        )
    }

    private companion object {
        const val PERSONALIZATION_SERVICE = "personalization-service"
    }
}
