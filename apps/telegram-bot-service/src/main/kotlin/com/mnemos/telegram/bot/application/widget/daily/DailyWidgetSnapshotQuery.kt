package com.mnemos.telegram.bot.application.widget.daily

interface DailyWidgetSnapshotQuery {
    fun load(telegramId: Long): DailyWidgetSnapshotResult
}
