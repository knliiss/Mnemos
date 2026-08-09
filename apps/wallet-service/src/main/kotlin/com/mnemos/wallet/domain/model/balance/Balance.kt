package com.mnemos.wallet.domain.model.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.model.transaction.TransactionId
import java.time.Instant

class Balance private constructor(
    val accountId: AccountId,
    val currency: Currency,
    amount: BalanceAmount,
    lastTransactionId: TransactionId?,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var updatedAt = updatedAt
        private set

    var lastTransactionId = lastTransactionId
        private set

    var amount = amount
        private set

    fun apply(change: BalanceChange) {
        require(change.accountId == accountId) {
            "Balance change belongs to another account."
        }

        require(change.currency == currency) {
            "Balance change belongs to another currency."
        }

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
            accountId: AccountId,
            currency: Currency,
            createdAt: Instant = Instant.now(),
        ): Balance =
            Balance(
                accountId = accountId,
                currency = currency,
                amount = BalanceAmount.ZERO,
                lastTransactionId = null,
                createdAt = createdAt,
                updatedAt = createdAt,
            )

        fun restore(
            accountId: AccountId,
            currency: Currency,
            amount: BalanceAmount,
            lastTransactionId: TransactionId?,
            createdAt: Instant,
            updatedAt: Instant,
        ): Balance =
            Balance(
                accountId = accountId,
                currency = currency,
                amount = amount,
                lastTransactionId = lastTransactionId,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
    }
}
