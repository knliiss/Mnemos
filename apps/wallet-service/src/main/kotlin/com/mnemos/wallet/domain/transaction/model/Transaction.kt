package com.mnemos.wallet.domain.transaction.model

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.balance.model.BalanceChange
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import java.time.Instant

class Transaction private constructor(
    val id: TransactionId,
    val owner: WalletOwner,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: TransactionSourceReferenceId,
    val amount: TransactionAmount,
    val requestId: RequestId,
    val createdAt: Instant,
) {
    init {
        WalletCurrencyPolicy.requireSupported(owner, currency)
    }

    fun toBalanceChange(): BalanceChange =
        BalanceChange(
            transactionId = id,
            owner = owner,
            currency = currency,
            type = type,
            amount = amount,
            appliedAt = createdAt,
        )

    companion object {
        fun create(
            owner: WalletOwner,
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
                owner = owner,
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
            owner: WalletOwner,
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
                owner = owner,
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
