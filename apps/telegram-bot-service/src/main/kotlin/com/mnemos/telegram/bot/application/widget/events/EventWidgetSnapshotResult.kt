package com.mnemos.telegram.bot.application.widget.events

sealed interface EventWidgetSnapshotResult {
    data class Available(
        val view: EventWidgetView,
    ) : EventWidgetSnapshotResult

    data object Unavailable : EventWidgetSnapshotResult
}
