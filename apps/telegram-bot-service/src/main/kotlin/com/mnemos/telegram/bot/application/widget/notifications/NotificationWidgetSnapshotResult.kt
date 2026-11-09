package com.mnemos.telegram.bot.application.widget.notifications

sealed interface NotificationWidgetSnapshotResult {
    data class Available(
        val view: NotificationWidgetView,
    ) : NotificationWidgetSnapshotResult

    data object Unavailable : NotificationWidgetSnapshotResult
}
