package com.mnemos.realtime.adapter.inbound.messaging.bet

import com.mnemos.bet.contracts.BetAggregateType
import com.mnemos.bet.contracts.BetEventContract
import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.BetOperationCompletedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.delivery.model.BetOperationCompletedPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventCommand
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import org.springframework.stereotype.Component

@Component
class BetOperationCompletedHandler(
    private val publishRealtimeEventUseCase: PublishRealtimeEventUseCase,
) : KafkaMessageHandler<BetOperationCompletedV1> {
    override val definition =
        kafkaMessageDefinition<BetOperationCompletedV1>(
            BetEventContract.TOPIC,
            BetEventType.OPERATION_COMPLETED,
            BetEventContract.VERSION,
            BetAggregateType.OPERATION,
        ) { envelope ->
            if (envelope.producer != BET_SERVICE) {
                throw KafkaMessageProtocolException("Bet results must be produced by bet-service")
            }

            if (envelope.aggregateId != envelope.payload.operationId) {
                throw KafkaMessageProtocolException("Bet result aggregateId must match operationId")
            }

            if (envelope.metadata.actorUserId != envelope.payload.userId) {
                throw KafkaMessageProtocolException("Bet result actor must match userId")
            }
        }

    override val consumer = "realtime-service.bet-operation-completed"

    override fun handle(message: ConsumedMessage<BetOperationCompletedV1>) {
        val event = message.payload

        publishRealtimeEventUseCase.execute(
            PublishRealtimeEventCommand(
                eventId = message.messageId,
                userId = event.userId,
                occurredAt = event.occurredAt,
                operationId = event.operationId,
                payload =
                    BetOperationCompletedPayload(
                        resourceType = event.resourceType.name,
                        resourceId = event.resourceId,
                        status = event.status.name,
                        amount = event.amount,
                        walletTransactionId = event.walletTransactionId,
                        failureReason = event.failureReason,
                    ),
            ),
        )
    }

    private companion object {
        const val BET_SERVICE = "bet-service"
    }
}
