package com.mnemos.wallet.contracts.wallet.internal

import com.mnemos.wallet.contracts.wallet.shared.Currency

data class TelegramAccountBalanceResponse(
    val currency: Currency,
    val amount: Long,
)
