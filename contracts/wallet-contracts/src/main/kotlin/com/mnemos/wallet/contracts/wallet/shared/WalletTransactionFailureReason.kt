package com.mnemos.wallet.contracts.wallet.shared

enum class WalletTransactionFailureReason {
    INSUFFICIENT_BALANCE,
    INVALID_AMOUNT,
    OWNER_CURRENCY_MISMATCH,
    BALANCE_LIMIT_EXCEEDED,
    IDEMPOTENCY_CONFLICT,
    PERMISSION_DENIED,
}
