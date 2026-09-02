package com.mnemos.telegram.bot.application.online.model

sealed interface SaoOnlineSnapshotResult {
    data class Available(
        val snapshot: SaoOnlineSnapshot,
    ) : SaoOnlineSnapshotResult

    data object Unavailable : SaoOnlineSnapshotResult
}
