package com.mnemos.wallet.application.transaction.policy

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType

class UnauthorizedWalletCommandException(
    producer: String,
    source: TransactionSource,
    type: TransactionType,
    currency: Currency,
) : RuntimeException(
        "Producer $producer cannot perform $type $source transaction in $currency",
    )
