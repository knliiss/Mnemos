package com.mnemos.wallet.domain.model.transaction

@JvmInline
value class TransactionAmount(
    val value: Long,
) {
    init {
        require(value > 0) {
            "Transaction amount must be positive."
        }
    }
}
