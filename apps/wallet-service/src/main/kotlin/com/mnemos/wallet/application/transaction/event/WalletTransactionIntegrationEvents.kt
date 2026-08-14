package com.mnemos.wallet.application.transaction.event

import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResponse
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.transaction.model.TransactionRequest
import java.time.Instant

object WalletTransactionIntegrationEvents {
    fun completed(
        response: CreateTransactionResponse,
        occurredAt: Instant,
    ): IntegrationEvent<WalletTransactionCompletedEvent> =
        IntegrationEvent(
            type = WalletEventType.TRANSACTION_COMPLETED,
            aggregateId = response.requestId.id,
            occurredAt = occurredAt,
            payload =
                WalletTransactionCompletedEvent(
                    requestId = response.requestId.id,
                    transactionId = response.transactionId.id,
                    owner = WalletOwner(response.owner.type, response.owner.id),
                    currency = response.currency,
                    type = response.type,
                    source = response.source,
                    sourceReferenceId = response.sourceReferenceId.id,
                    amount = response.amount,
                    occurredAt = occurredAt,
                ),
        )

    fun failed(
        request: TransactionRequest,
        reason: WalletTransactionFailureReason,
        occurredAt: Instant,
    ): IntegrationEvent<WalletTransactionFailedEvent> =
        IntegrationEvent(
            type = WalletEventType.TRANSACTION_FAILED,
            aggregateId = request.requestId.id,
            occurredAt = occurredAt,
            payload =
                WalletTransactionFailedEvent(
                    requestId = request.requestId.id,
                    owner = WalletOwner(request.owner.type, request.owner.id),
                    currency = request.currency,
                    type = request.type,
                    source = request.source,
                    sourceReferenceId = request.sourceReferenceId.id,
                    amount = request.amount,
                    reason = reason,
                    occurredAt = occurredAt,
                ),
        )
}
