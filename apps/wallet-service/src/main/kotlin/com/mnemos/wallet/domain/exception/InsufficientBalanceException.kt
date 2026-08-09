package com.mnemos.wallet.domain.exception

import com.mnemos.wallet.domain.model.balance.BalanceAmount
import com.mnemos.wallet.domain.model.transaction.TransactionAmount

class InsufficientBalanceException(
    val available: BalanceAmount,
    val required: TransactionAmount,
) : RuntimeException(
        "Insufficient balance: available=${available.value}, required=${required.value}",
    )
