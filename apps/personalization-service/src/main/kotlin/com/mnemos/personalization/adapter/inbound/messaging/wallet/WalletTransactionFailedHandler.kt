package com.mnemos.personalization.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.application.purchase.ProcessPurchaseWalletResultService
import com.mnemos.personalization.application.revenue.ProcessCreatorRevenueWalletResultService
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import org.springframework.stereotype.Component

@Component
class WalletTransactionFailedHandler(
    private val purchaseService: ProcessPurchaseWalletResultService,
    private val creatorRevenueService: ProcessCreatorRevenueWalletResultService,
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
        }

    override val consumer = "personalization-service.wallet-failed"

    override fun handle(message: ConsumedMessage<WalletTransactionFailedEvent>) {
        val payload = message.payload

        when (payload.source) {
            TransactionSource.PERSONALIZATION_PURCHASE ->
                purchaseService.fail(
                    requestId = payload.requestId,
                    reason = payload.reason.name,
                )

            TransactionSource.CREATOR_REVENUE ->
                creatorRevenueService.fail(
                    requestId = payload.requestId,
                    reason = payload.reason.name,
                )

            else -> Unit
        }
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
