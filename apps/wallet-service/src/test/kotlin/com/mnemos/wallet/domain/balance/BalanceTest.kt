package com.mnemos.wallet.domain.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.exception.InsufficientBalanceException
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.Transaction
import com.mnemos.wallet.domain.transaction.model.TransactionAmount
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BalanceTest {
    @Test
    fun `credit and debit update balance`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val balance = Balance.create(owner = owner, currency = Currency.COIN)
        val credit = transaction(owner, TransactionType.CREDIT, 100)
        val debit = transaction(owner, TransactionType.DEBIT, 40)

        balance.apply(credit.toBalanceChange())
        balance.apply(debit.toBalanceChange())

        assertEquals(60, balance.amount.value)
        assertEquals(debit.id, balance.lastTransactionId)
    }

    @Test
    fun `debit cannot make balance negative`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val balance = Balance.create(owner = owner, currency = Currency.COIN)
        val debit = transaction(owner, TransactionType.DEBIT, 1)

        assertThrows(InsufficientBalanceException::class.java) {
            balance.apply(debit.toBalanceChange())
        }
    }

    private fun transaction(
        owner: WalletOwner,
        type: TransactionType,
        amount: Long,
    ): Transaction =
        Transaction.create(
            owner = owner,
            currency = Currency.COIN,
            type = type,
            source = TransactionSource.ADMIN_ADJUSTMENT,
            sourceReferenceId = TransactionSourceReferenceId(AccountId.generate().id),
            amount = TransactionAmount(amount),
            requestId = RequestId.generate(),
        )
}
