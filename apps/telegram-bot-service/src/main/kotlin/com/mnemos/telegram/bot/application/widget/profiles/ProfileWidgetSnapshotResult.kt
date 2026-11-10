package com.mnemos.telegram.bot.application.widget.profiles

sealed interface ProfileWidgetSnapshotResult {
    data class Available(val view: ProfileWidgetView) : ProfileWidgetSnapshotResult

    data object Unavailable : ProfileWidgetSnapshotResult

    data object NotFound : ProfileWidgetSnapshotResult
}
