package com.mnemos.telegram.bot.application.widget.bets

sealed interface BetWidgetSnapshotResult {
    data class Available(
        val view: BetWidgetView,
    ) : BetWidgetSnapshotResult

    data object Unavailable : BetWidgetSnapshotResult
}
