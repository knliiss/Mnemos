package com.mnemos.wallet.domain.model.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.model.balance.BalanceChange
import java.time.Instant

class Transaction private constructor(
    val id: TransactionId,
    val accountId: AccountId,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: TransactionSourceReferenceId,
    val amount: TransactionAmount,
    val requestId: RequestId,
    val createdAt: Instant,
) {
    fun toBalanceChange(): BalanceChange =
        BalanceChange(
            transactionId = id,
            accountId = accountId,
            currency = currency,
            type = type,
            amount = amount,
            appliedAt = createdAt,
        )

    companion object {
        fun create(
            accountId: AccountId,
            currency: Currency,
            type: TransactionType,
            source: TransactionSource,
            sourceReferenceId: TransactionSourceReferenceId,
            amount: TransactionAmount,
            requestId: RequestId,
            createdAt: Instant = Instant.now(),
        ): Transaction =
            Transaction(
                id = TransactionId.generate(),
                accountId = accountId,
                currency = currency,
                type = type,
                source = source,
                sourceReferenceId = sourceReferenceId,
                amount = amount,
                requestId = requestId,
                createdAt = createdAt,
            )

        fun restore(
            id: TransactionId,
            accountId: AccountId,
            currency: Currency,
            type: TransactionType,
            source: TransactionSource,
            sourceReferenceId: TransactionSourceReferenceId,
            amount: TransactionAmount,
            requestId: RequestId,
            createdAt: Instant,
        ): Transaction =
            Transaction(
                id = id,
                accountId = accountId,
                currency = currency,
                type = type,
                source = source,
                sourceReferenceId = sourceReferenceId,
                amount = amount,
                requestId = requestId,
                createdAt = createdAt,
            )
    }
}
