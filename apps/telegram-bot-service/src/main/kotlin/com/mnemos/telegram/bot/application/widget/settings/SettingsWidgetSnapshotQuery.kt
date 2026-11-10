package com.mnemos.telegram.bot.application.widget.settings

interface SettingsWidgetSnapshotQuery {
    fun load(telegramId: Long, page: Int): SettingsWidgetSnapshotResult
}
