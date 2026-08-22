package com.mnemos.realtime.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwner
import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwnerType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.application.delivery.port.out.WalletRealtimeRecipientResolver
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventCommand
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component

@Component
class WalletTransactionCompletedHandler(
    private val publishRealtimeEventUseCase: PublishRealtimeEventUseCase,
    private val recipientResolver: WalletRealtimeRecipientResolver,
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
        val recipients = recipientResolver.resolve(event.owner.toRealtimeOwner())

        check(recipients.isNotEmpty()) {
            "Wallet ${event.owner.type}/${event.owner.id} has no realtime recipients"
        }

        val payload =
            WalletTransactionCompletedPayload(
                requestId = event.requestId,
                transactionId = event.transactionId,
                ownerType = event.owner.type.name,
                ownerId = event.owner.id,
                currency = event.currency.name,
                source = event.source.name,
                sourceReferenceId = event.sourceReferenceId,
                amount = event.amount,
            )

        recipients.forEach { userId ->
            publishRealtimeEventUseCase.execute(
                PublishRealtimeEventCommand(
                    eventId = message.messageId,
                    userId = userId,
                    occurredAt = event.occurredAt,
                    operationId = null,
                    payload = payload,
                ),
            )
        }
    }

    private fun WalletOwner.toRealtimeOwner(): WalletRealtimeOwner =
        WalletRealtimeOwner(
            type =
                when (type) {
                    WalletOwnerType.USER -> WalletRealtimeOwnerType.USER
                    WalletOwnerType.ACCOUNT -> WalletRealtimeOwnerType.ACCOUNT
                },
            id = id,
        )

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
