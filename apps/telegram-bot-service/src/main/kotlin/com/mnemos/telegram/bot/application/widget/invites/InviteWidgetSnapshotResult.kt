package com.mnemos.telegram.bot.application.widget.invites

sealed interface InviteWidgetSnapshotResult {
    data class Available(val view: InviteWidgetView) : InviteWidgetSnapshotResult
    data object AccountRequired : InviteWidgetSnapshotResult
    data object Forbidden : InviteWidgetSnapshotResult
    data object Unavailable : InviteWidgetSnapshotResult
}
