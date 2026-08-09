package com.mnemos.wallet.domain

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.exception.InsufficientBalanceException
import com.mnemos.wallet.domain.model.balance.Balance
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.Transaction
import com.mnemos.wallet.domain.model.transaction.TransactionAmount
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class BalanceTest {
    @Test
    fun `credit and debit update balance and last transaction`() {
        val accountId = AccountId.generate()
        val balance = Balance.create(accountId, Currency.COIN)
        val credit =
            transaction(
                accountId = accountId,
                type = TransactionType.CREDIT,
                amount = 100,
            )
        val debit =
            transaction(
                accountId = accountId,
                type = TransactionType.DEBIT,
                amount = 40,
            )

        balance.apply(credit.toBalanceChange())
        balance.apply(debit.toBalanceChange())

        assertEquals(60, balance.amount.value)
        assertEquals(debit.id, balance.lastTransactionId)
        assertEquals(debit.createdAt, balance.updatedAt)
    }

    @Test
    fun `debit cannot make balance negative`() {
        val accountId = AccountId.generate()
        val balance = Balance.create(accountId, Currency.COIN)
        val debit =
            transaction(
                accountId = accountId,
                type = TransactionType.DEBIT,
                amount = 1,
            )

        assertThrows<InsufficientBalanceException> {
            balance.apply(debit.toBalanceChange())
        }

        assertEquals(0, balance.amount.value)
        assertEquals(null, balance.lastTransactionId)
    }

    @Test
    fun `balance rejects change for another currency`() {
        val accountId = AccountId.generate()
        val balance = Balance.create(accountId, Currency.PRIME)
        val credit =
            transaction(
                accountId = accountId,
                type = TransactionType.CREDIT,
                amount = 100,
                currency = Currency.COIN,
            )

        assertThrows<IllegalArgumentException> {
            balance.apply(credit.toBalanceChange())
        }
    }

    private fun transaction(
        accountId: AccountId,
        type: TransactionType,
        amount: Long,
        currency: Currency = Currency.COIN,
    ): Transaction =
        Transaction.create(
            accountId = accountId,
            currency = currency,
            type = type,
            source = TransactionSource.ADMIN_ADJUSTMENT,
            sourceReferenceId = TransactionSourceReferenceId(UUID.randomUUID()),
            amount = TransactionAmount(amount),
            requestId = RequestId(UUID.randomUUID()),
            createdAt = Instant.now(),
        )
}
