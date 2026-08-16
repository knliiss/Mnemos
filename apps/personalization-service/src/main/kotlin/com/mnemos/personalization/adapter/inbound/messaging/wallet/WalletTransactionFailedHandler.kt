package com.mnemos.personalization.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.application.purchase.usecase.failwallet.FailPurchaseWalletUseCase
import com.mnemos.personalization.application.revenue.usecase.failwallet.FailCreatorRevenueWalletUseCase
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import org.springframework.stereotype.Component

@Component
class WalletTransactionFailedHandler(
    private val failPurchaseWalletUseCase: FailPurchaseWalletUseCase,
    private val failCreatorRevenueWalletUseCase: FailCreatorRevenueWalletUseCase,
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
                throw KafkaMessageProtocolException("Wallet result occurredAt must match envelope occurredAt")
            }
        }

    override val consumer = "personalization-service.wallet-failed"

    override fun handle(message: ConsumedMessage<WalletTransactionFailedEvent>) {
        val payload = message.payload
        val result = payload.toResultContext(message)

        when (payload.source) {
            TransactionSource.PERSONALIZATION_PURCHASE ->
                failPurchaseWalletUseCase.execute(
                    result = result,
                    reason = payload.reason.name,
                )

            TransactionSource.CREATOR_REVENUE ->
                failCreatorRevenueWalletUseCase.execute(
                    result = result,
                    reason = payload.reason.name,
                )

            else -> Unit
        }
    }

    private fun WalletTransactionFailedEvent.toResultContext(
        message: ConsumedMessage<WalletTransactionFailedEvent>,
    ): WalletResultContext =
        WalletResultContext(
            requestId = requestId,
            actorUserId = message.metadata.actorUserId,
            ownerType = owner.type,
            ownerId = owner.id,
            currency = currency,
            transactionType = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
        )

    private companion object {
        const val WALLET_SERVICE = "wallet-service"
    }
}
