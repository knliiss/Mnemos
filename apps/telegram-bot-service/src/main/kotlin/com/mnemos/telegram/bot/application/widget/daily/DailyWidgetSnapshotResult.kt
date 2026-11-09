package com.mnemos.telegram.bot.application.widget.daily

sealed interface DailyWidgetSnapshotResult {
    data class Available(
        val view: DailyWidgetView,
    ) : DailyWidgetSnapshotResult

    data object Unavailable : DailyWidgetSnapshotResult
}
