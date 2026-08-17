package com.mnemos.moderation.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.moderation.application.wallet.usecase.result.WalletAdjustmentResultService
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import org.springframework.stereotype.Component

@Component
class WalletTransactionCompletedHandler(
    private val resultService: WalletAdjustmentResultService,
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

    override val consumer = "moderation-service.wallet-adjustment-completed"

    override fun handle(message: ConsumedMessage<WalletTransactionCompletedEvent>) {
        resultService.complete(message.payload)
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
