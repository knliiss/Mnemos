package com.mnemos.wallet.adapter.inbound.http.balance

import com.mnemos.wallet.contracts.wallet.shared.Currency

data class BalanceResponse(
    val currency: Currency,
    val amount: Long,
)
