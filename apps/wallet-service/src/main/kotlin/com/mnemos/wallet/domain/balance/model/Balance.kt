package com.mnemos.wallet.domain.balance.model

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.transaction.model.TransactionId
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import java.time.Instant

class Balance private constructor(
    val owner: WalletOwner,
    val currency: Currency,
    amount: BalanceAmount,
    lastTransactionId: TransactionId?,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var amount = amount
        private set

    var lastTransactionId = lastTransactionId
        private set

    var updatedAt = updatedAt
        private set

    init {
        WalletCurrencyPolicy.requireSupported(owner, currency)
    }

    fun apply(change: BalanceChange) {
        require(change.owner == owner) { "Balance change belongs to another wallet owner." }
        require(change.currency == currency) { "Balance change belongs to another currency." }

        amount =
            when (change.type) {
                TransactionType.CREDIT -> amount + change.amount
                TransactionType.DEBIT -> amount - change.amount
            }

        lastTransactionId = change.transactionId
        updatedAt = change.appliedAt
    }

    companion object {
        fun create(
            owner: WalletOwner,
            currency: Currency,
            createdAt: Instant = Instant.now(),
        ): Balance =
            Balance(
                owner = owner,
                currency = currency,
                amount = BalanceAmount.ZERO,
                lastTransactionId = null,
                createdAt = createdAt,
                updatedAt = createdAt,
            )

        fun restore(
            owner: WalletOwner,
            currency: Currency,
            amount: BalanceAmount,
            lastTransactionId: TransactionId?,
            createdAt: Instant,
            updatedAt: Instant,
        ): Balance =
            Balance(
                owner = owner,
                currency = currency,
                amount = amount,
                lastTransactionId = lastTransactionId,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
    }
}
