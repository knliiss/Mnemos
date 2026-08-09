package com.mnemos.wallet.domain.exception

import com.mnemos.wallet.domain.model.balance.BalanceAmount
import com.mnemos.wallet.domain.model.transaction.TransactionAmount

class BalanceLimitExceededException(
    val current: BalanceAmount,
    val increment: TransactionAmount,
) : RuntimeException(
        "Balance limit exceeded: current=${current.value}, increment=${increment.value}",
    )
