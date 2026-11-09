package com.mnemos.telegram.bot.application.widget.online

interface OnlineWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        realmNumber: Int?,
    ): OnlineWidgetSnapshotResult
}
