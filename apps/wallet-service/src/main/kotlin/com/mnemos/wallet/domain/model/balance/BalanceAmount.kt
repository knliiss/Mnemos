package com.mnemos.wallet.domain.model.balance

import com.mnemos.wallet.domain.exception.BalanceLimitExceededException
import com.mnemos.wallet.domain.exception.InsufficientBalanceException
import com.mnemos.wallet.domain.model.transaction.TransactionAmount

@JvmInline
value class BalanceAmount(
    val value: Long,
) : Comparable<BalanceAmount> {
    operator fun plus(other: TransactionAmount): BalanceAmount =
        try {
            BalanceAmount(Math.addExact(value, other.value))
        } catch (_: ArithmeticException) {
            throw BalanceLimitExceededException(
                current = this,
                increment = other,
            )
        }

    operator fun minus(other: TransactionAmount): BalanceAmount {
        if (value < other.value) {
            throw InsufficientBalanceException(
                available = this,
                required = other,
            )
        }

        return BalanceAmount(value - other.value)
    }

    override fun compareTo(other: BalanceAmount): Int = value.compareTo(other.value)

    init {
        require(value >= 0) {
            "Balance amount cannot be negative."
        }
    }

    companion object {
        val ZERO = BalanceAmount(0)
    }
}
