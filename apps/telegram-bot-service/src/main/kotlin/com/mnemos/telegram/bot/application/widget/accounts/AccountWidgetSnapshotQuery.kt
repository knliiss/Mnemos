package com.mnemos.telegram.bot.application.widget.accounts

interface AccountWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        page: Int,
    ): AccountWidgetSnapshotResult
}

