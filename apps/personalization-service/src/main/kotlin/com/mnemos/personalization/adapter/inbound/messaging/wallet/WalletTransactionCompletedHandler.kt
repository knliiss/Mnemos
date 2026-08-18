package com.mnemos.personalization.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.adapter.inbound.messaging.wallet.WalletResultContextMapper.toResultContext
import com.mnemos.personalization.application.purchase.usecase.completewallet.CompletePurchaseWalletUseCase
import com.mnemos.personalization.application.revenue.usecase.completewallet.CompleteCreatorRevenueWalletUseCase
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import org.springframework.stereotype.Component

@Component
class WalletTransactionCompletedHandler(
    private val completePurchaseWalletUseCase: CompletePurchaseWalletUseCase,
    private val completeCreatorRevenueWalletUseCase: CompleteCreatorRevenueWalletUseCase,
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

    override val consumer = "personalization-service.wallet-completed"

    override fun handle(message: ConsumedMessage<WalletTransactionCompletedEvent>) {
        val payload = message.payload
        val result = payload.toResultContext(message)

        when (payload.source) {
            TransactionSource.PERSONALIZATION_PURCHASE ->
                completePurchaseWalletUseCase.execute(
                    result = result,
                    transactionId = payload.transactionId,
                )

            TransactionSource.CREATOR_REVENUE ->
                completeCreatorRevenueWalletUseCase.execute(
                    result = result,
                    transactionId = payload.transactionId,
                )

            else -> Unit
        }
    }

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
