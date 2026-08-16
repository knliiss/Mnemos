package com.mnemos.social.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.social.adapter.inbound.messaging.wallet.SpotlightWalletResultMessageMapper.toSpotlightResult
import com.mnemos.social.application.spotlight.ProcessDropSpotlightWalletResultService
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import org.springframework.stereotype.Component

@Component
class WalletTransactionCompletedHandler(
    private val service: ProcessDropSpotlightWalletResultService,
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

            if (envelope.occurredAt != envelope.payload.occurredAt) {
                throw KafkaMessageProtocolException("Wallet result occurredAt must match envelope occurredAt")
            }
        }

    override val consumer = "social-service.wallet-completed"

    override fun handle(message: ConsumedMessage<WalletTransactionCompletedEvent>) {
        val event = message.payload
        val result = event.toSpotlightResult() ?: return

        service.completed(
            result = result,
            transactionId = event.transactionId,
        )
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
