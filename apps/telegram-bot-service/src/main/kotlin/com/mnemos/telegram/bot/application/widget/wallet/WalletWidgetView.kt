package com.mnemos.telegram.bot.application.widget.wallet

import java.util.UUID

data class WalletWidgetView(
    val accountId: UUID?,
    val coin: Long?,
    val prime: Long?,
    val page: Int,
    val totalPages: Int,
    val totalTransactions: Long,
    val historyStatus: WalletHistoryStatus,
    val entries: List<WalletTransactionEntry>,
)
