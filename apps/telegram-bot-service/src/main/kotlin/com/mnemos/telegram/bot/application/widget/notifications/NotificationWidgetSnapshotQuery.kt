package com.mnemos.telegram.bot.application.widget.notifications

interface NotificationWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        page: Int,
    ): NotificationWidgetSnapshotResult
}

sealed interface NotificationWidgetSnapshotResult {
    data class Available(
        val view: NotificationWidgetView,
    ) : NotificationWidgetSnapshotResult

    data object Unavailable : NotificationWidgetSnapshotResult
}
