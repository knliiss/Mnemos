package com.mnemos.wallet.domain.transaction.model

@JvmInline
value class TransactionAmount(
    val value: Long,
) {
    init {
        require(value > 0) { "Transaction amount must be greater than zero." }
    }
}
