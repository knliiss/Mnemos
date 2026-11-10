package com.mnemos.telegram.bot.application.widget.online

sealed interface OnlineWidgetSnapshotResult {
    data class Available(
        val view: OnlineWidgetView,
    ) : OnlineWidgetSnapshotResult

    data object Unavailable : OnlineWidgetSnapshotResult
}
