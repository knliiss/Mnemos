package com.mnemos.social.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.social.application.spotlight.ProcessDropSpotlightWalletResultService
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import org.springframework.stereotype.Component

@Component
class WalletTransactionFailedHandler(
    private val service: ProcessDropSpotlightWalletResultService,
) : KafkaMessageHandler<WalletTransactionFailedEvent> {
    override val definition =
        kafkaMessageDefinition<WalletTransactionFailedEvent>(
            WalletEventContract.TOPIC,
            WalletEventType.TRANSACTION_FAILED,
            WalletEventContract.VERSION,
            WalletEventContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != WALLET_SERVICE) {
                throw KafkaMessageProtocolException("Wallet results must be produced by wallet-service")
            }
            if (envelope.aggregateId != envelope.payload.requestId) {
                throw KafkaMessageProtocolException("Wallet result aggregateId must match requestId")
            }
            if (envelope.occurredAt != envelope.payload.occurredAt) {
                throw KafkaMessageProtocolException("Wallet result occurredAt must match envelope")
            }
        }

    override val consumer = "social-service.wallet-failed"

    override fun handle(message: ConsumedMessage<WalletTransactionFailedEvent>) {
        val event = message.payload
        if (event.source != TransactionSource.SPOTLIGHT_PURCHASE) {
            return
        }

        service.failed(event.sourceReferenceId)
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
