package com.mnemos.wallet.application.balance.model

import com.mnemos.wallet.contracts.wallet.shared.Currency

data class BalanceView(
    val currency: Currency,
    val amount: Long,
)
