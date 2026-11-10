package com.mnemos.telegram.bot.application.widget.wallet

interface WalletWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        page: Int,
    ): WalletWidgetSnapshotResult
}
