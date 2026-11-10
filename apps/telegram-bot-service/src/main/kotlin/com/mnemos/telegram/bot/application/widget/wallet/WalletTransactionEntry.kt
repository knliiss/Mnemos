package com.mnemos.telegram.bot.application.widget.wallet

data class WalletTransactionEntry(
    val label: String,
    val amount: Long,
    val currency: String,
    val createdAt: String,
)
