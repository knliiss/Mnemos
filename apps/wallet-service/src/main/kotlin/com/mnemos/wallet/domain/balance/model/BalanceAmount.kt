package com.mnemos.wallet.domain.balance.model

import com.mnemos.wallet.domain.exception.BalanceLimitExceededException
import com.mnemos.wallet.domain.exception.InsufficientBalanceException
import com.mnemos.wallet.domain.transaction.model.TransactionAmount

@JvmInline
value class BalanceAmount(
    val value: Long,
) {
    operator fun plus(amount: TransactionAmount): BalanceAmount {
        val result =
            try {
                Math.addExact(value, amount.value)
            } catch (_: ArithmeticException) {
                throw BalanceLimitExceededException()
            }

        return BalanceAmount(result)
    }

    operator fun minus(amount: TransactionAmount): BalanceAmount {
        if (value < amount.value) {
            throw InsufficientBalanceException()
        }

        return BalanceAmount(value - amount.value)
    }

    companion object {
        val ZERO = BalanceAmount(0)
    }
}
