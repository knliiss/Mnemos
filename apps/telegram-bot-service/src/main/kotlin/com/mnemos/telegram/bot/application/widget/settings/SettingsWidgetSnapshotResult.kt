package com.mnemos.telegram.bot.application.widget.settings

sealed interface SettingsWidgetSnapshotResult {
    data class Available(val view: SettingsWidgetView) : SettingsWidgetSnapshotResult
    data object Unavailable : SettingsWidgetSnapshotResult
}
