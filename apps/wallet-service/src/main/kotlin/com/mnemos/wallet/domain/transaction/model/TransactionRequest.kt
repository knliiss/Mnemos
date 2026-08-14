package com.mnemos.wallet.domain.transaction.model

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import java.time.Instant

class TransactionRequest private constructor(
    val requestId: RequestId,
    val owner: WalletOwner,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: TransactionSourceReferenceId,
    val amount: Long,
    status: TransactionRequestStatus,
    transactionId: TransactionId?,
    failureReason: WalletTransactionFailureReason?,
    val createdAt: Instant,
    completedAt: Instant?,
) {
    var status = status
        private set

    var transactionId = transactionId
        private set

    var failureReason = failureReason
        private set

    var completedAt = completedAt
        private set

    fun matches(
        owner: WalletOwner,
        currency: Currency,
        type: TransactionType,
        source: TransactionSource,
        sourceReferenceId: TransactionSourceReferenceId,
        amount: Long,
    ): Boolean =
        this.owner == owner &&
            this.currency == currency &&
            this.type == type &&
            this.source == source &&
            this.sourceReferenceId == sourceReferenceId &&
            this.amount == amount

    fun complete(
        transactionId: TransactionId,
        completedAt: Instant,
    ) {
        check(status == TransactionRequestStatus.PENDING) { "Only pending transaction request can be completed." }

        status = TransactionRequestStatus.COMPLETED
        this.transactionId = transactionId
        failureReason = null
        this.completedAt = completedAt
    }

    fun fail(
        reason: WalletTransactionFailureReason,
        completedAt: Instant,
    ) {
        check(status == TransactionRequestStatus.PENDING) { "Only pending transaction request can fail." }

        status = TransactionRequestStatus.FAILED
        transactionId = null
        failureReason = reason
        this.completedAt = completedAt
    }

    companion object {
        fun create(
            requestId: RequestId,
            owner: WalletOwner,
            currency: Currency,
            type: TransactionType,
            source: TransactionSource,
            sourceReferenceId: TransactionSourceReferenceId,
            amount: Long,
            createdAt: Instant = Instant.now(),
        ): TransactionRequest =
            TransactionRequest(
                requestId = requestId,
                owner = owner,
                currency = currency,
                type = type,
                source = source,
                sourceReferenceId = sourceReferenceId,
                amount = amount,
                status = TransactionRequestStatus.PENDING,
                transactionId = null,
                failureReason = null,
                createdAt = createdAt,
                completedAt = null,
            )

        fun restore(
            requestId: RequestId,
            owner: WalletOwner,
            currency: Currency,
            type: TransactionType,
            source: TransactionSource,
            sourceReferenceId: TransactionSourceReferenceId,
            amount: Long,
            status: TransactionRequestStatus,
            transactionId: TransactionId?,
            failureReason: WalletTransactionFailureReason?,
            createdAt: Instant,
            completedAt: Instant?,
        ): TransactionRequest =
            TransactionRequest(
                requestId = requestId,
                owner = owner,
                currency = currency,
                type = type,
                source = source,
                sourceReferenceId = sourceReferenceId,
                amount = amount,
                status = status,
                transactionId = transactionId,
                failureReason = failureReason,
                createdAt = createdAt,
                completedAt = completedAt,
            )
    }
}
