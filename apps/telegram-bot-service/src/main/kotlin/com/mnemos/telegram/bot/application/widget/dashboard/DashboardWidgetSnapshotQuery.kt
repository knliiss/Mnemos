package com.mnemos.telegram.bot.application.widget.dashboard

interface DashboardWidgetSnapshotQuery {
    fun load(telegramId: Long): DashboardWidgetSnapshotResult
}
