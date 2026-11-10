package com.mnemos.telegram.bot.application.widget.profilecomments

sealed interface ProfileCommentsWidgetSnapshotResult {
    data class Available(val view: ProfileCommentsWidgetView) : ProfileCommentsWidgetSnapshotResult
    data object NotFound : ProfileCommentsWidgetSnapshotResult
    data object Unavailable : ProfileCommentsWidgetSnapshotResult
}
