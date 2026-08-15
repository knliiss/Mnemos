package com.mnemos.personalization.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.application.purchase.ProcessPurchaseWalletResultService
import com.mnemos.personalization.application.revenue.ProcessCreatorRevenueWalletResultService
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import org.springframework.stereotype.Component

@Component
class WalletTransactionCompletedHandler(
    private val purchaseService: ProcessPurchaseWalletResultService,
    private val creatorRevenueService: ProcessCreatorRevenueWalletResultService,
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

    override val consumer = "personalization-service.wallet-completed"

    override fun handle(message: ConsumedMessage<WalletTransactionCompletedEvent>) {
        val payload = message.payload

        when (payload.source) {
            TransactionSource.PERSONALIZATION_PURCHASE ->
                purchaseService.complete(
                    requestId = payload.requestId,
                    transactionId = payload.transactionId,
                )

            TransactionSource.CREATOR_REVENUE ->
                creatorRevenueService.complete(
                    requestId = payload.requestId,
                    transactionId = payload.transactionId,
                )

            else -> Unit
        }
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
