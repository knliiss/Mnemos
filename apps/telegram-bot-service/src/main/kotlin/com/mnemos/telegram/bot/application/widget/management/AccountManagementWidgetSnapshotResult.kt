package com.mnemos.telegram.bot.application.widget.management

sealed interface AccountManagementWidgetSnapshotResult {
    data class Available(val view: AccountManagementWidgetView) : AccountManagementWidgetSnapshotResult
    data object AccountRequired : AccountManagementWidgetSnapshotResult
    data object Forbidden : AccountManagementWidgetSnapshotResult
    data object Unavailable : AccountManagementWidgetSnapshotResult
}
