package com.mnemos.telegram.bot.application.widget.profileactivity

sealed interface ProfileActivitySnapshotResult {
    data class Available(val view: ProfileActivityView) : ProfileActivitySnapshotResult
    data object NotFound : ProfileActivitySnapshotResult
    data object Unavailable : ProfileActivitySnapshotResult
}
