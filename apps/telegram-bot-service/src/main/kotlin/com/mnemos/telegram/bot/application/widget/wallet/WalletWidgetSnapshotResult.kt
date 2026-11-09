package com.mnemos.telegram.bot.application.widget.wallet

sealed interface WalletWidgetSnapshotResult {
    data class Available(
        val view: WalletWidgetView,
    ) : WalletWidgetSnapshotResult

    data object Unavailable : WalletWidgetSnapshotResult
}
