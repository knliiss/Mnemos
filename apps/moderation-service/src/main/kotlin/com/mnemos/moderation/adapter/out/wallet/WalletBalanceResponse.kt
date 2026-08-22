package com.mnemos.moderation.adapter.out.wallet

import com.mnemos.wallet.contracts.wallet.shared.Currency

data class WalletBalanceResponse(
    val currency: Currency,
    val amount: Long,
)
