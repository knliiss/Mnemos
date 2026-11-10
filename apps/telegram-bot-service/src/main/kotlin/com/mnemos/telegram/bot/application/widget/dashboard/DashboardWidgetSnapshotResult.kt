package com.mnemos.telegram.bot.application.widget.dashboard

sealed interface DashboardWidgetSnapshotResult {
    data class Available(
        val view: DashboardWidgetView,
    ) : DashboardWidgetSnapshotResult

    data object Unavailable : DashboardWidgetSnapshotResult
}
