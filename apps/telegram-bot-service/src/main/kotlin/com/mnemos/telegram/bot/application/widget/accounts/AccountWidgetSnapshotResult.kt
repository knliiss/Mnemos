package com.mnemos.telegram.bot.application.widget.accounts
sealed interface AccountWidgetSnapshotResult {
    data class Available(
        val view: AccountWidgetView,
    ) : AccountWidgetSnapshotResult

    data object Unavailable : AccountWidgetSnapshotResult
}
