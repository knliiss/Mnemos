package com.mnemos.telegram.bot.application.widget.dashboard

interface DashboardWidgetSnapshotQuery {
    fun load(telegramId: Long): DashboardWidgetSnapshotResult
}

sealed interface DashboardWidgetSnapshotResult {
    data class Available(
        val view: DashboardWidgetView,
    ) : DashboardWidgetSnapshotResult

    data object Unavailable : DashboardWidgetSnapshotResult
}
