package com.mnemos.telegram.bot.application.widget.notifications

interface NotificationWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        page: Int,
    ): NotificationWidgetSnapshotResult
}
