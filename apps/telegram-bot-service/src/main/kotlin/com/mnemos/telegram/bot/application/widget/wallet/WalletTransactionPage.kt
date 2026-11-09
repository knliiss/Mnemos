package com.mnemos.telegram.bot.application.widget.wallet

import java.util.UUID

data class WalletTransactionPage(
    val accountId: UUID,
    val entries: List<WalletTransactionEntry>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
)
