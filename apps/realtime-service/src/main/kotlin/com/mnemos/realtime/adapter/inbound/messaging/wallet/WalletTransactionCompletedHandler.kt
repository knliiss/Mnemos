package com.mnemos.realtime.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventCommand
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.springframework.stereotype.Component

@Component
class WalletTransactionCompletedHandler(
    private val publishRealtimeEventUseCase: PublishRealtimeEventUseCase,
) : KafkaMessageHandler<WalletTransactionCompletedEvent> {
    override val definition =
        kafkaMessageDefinition<WalletTransactionCompletedEvent>(
            WalletEventContract.TOPIC,
            WalletEventType.TRANSACTION_COMPLETED,
            WalletEventContract.VERSION,
            WalletEventContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != WALLET_SERVICE) {
                throw KafkaMessageProtocolException("Wallet results must be produced by wallet-service")
            }

            if (envelope.aggregateId != envelope.payload.requestId) {
                throw KafkaMessageProtocolException("Wallet result aggregateId must match requestId")
            }
        }

    override val consumer = "realtime-service.wallet-completed"

    override fun handle(message: ConsumedMessage<WalletTransactionCompletedEvent>) {
        val event = message.payload

        if (event.type != TransactionType.DEBIT) {
            return
        }

        val userId =
            message.metadata.actorUserId
                ?: throw KafkaMessageProtocolException("Debit wallet result must contain actorUserId")

        publishRealtimeEventUseCase.execute(
            PublishRealtimeEventCommand(
                eventId = message.messageId,
                userId = userId,
                occurredAt = event.occurredAt,
                operationId = null,
                payload =
                    WalletTransactionCompletedPayload(
                        requestId = event.requestId,
                        transactionId = event.transactionId,
                        ownerType = event.owner.type.name,
                        ownerId = event.owner.id,
                        currency = event.currency.name,
                        source = event.source.name,
                        sourceReferenceId = event.sourceReferenceId,
                        amount = event.amount,
                    ),
            ),
        )
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
