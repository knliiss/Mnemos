package com.mnemos.wallet.application.event

import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.wallet.application.transaction.CreateTransactionResponse
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.model.request.TransactionRequest
import java.time.Instant

object WalletTransactionIntegrationEvents {
    fun completed(
        response: CreateTransactionResponse,
        occurredAt: Instant,
    ): IntegrationEvent<WalletTransactionCompletedEvent> =
        IntegrationEvent(
            type =
                WalletEventType.TRANSACTION_COMPLETED,
            aggregateId = response.requestId.id,
            occurredAt = occurredAt,
            payload =
                WalletTransactionCompletedEvent(
                    requestId = response.requestId.id,
                    transactionId =
                        response.transactionId.id,
                    accountId = response.accountId.id,
                    sourceReferenceId =
                        response.sourceReferenceId.id,
                ),
        )

    fun failed(
        request: TransactionRequest,
        reason: WalletTransactionFailureReason,
        occurredAt: Instant,
    ): IntegrationEvent<WalletTransactionFailedEvent> =
        IntegrationEvent(
            type =
                WalletEventType.TRANSACTION_FAILED,
            aggregateId = request.requestId.id,
            occurredAt = occurredAt,
            payload =
                WalletTransactionFailedEvent(
                    requestId = request.requestId.id,
                    reason = reason,
                ),
        )
}
