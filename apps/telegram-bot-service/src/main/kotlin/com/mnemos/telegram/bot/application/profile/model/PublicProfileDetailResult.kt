package com.mnemos.telegram.bot.application.profile.model

sealed interface PublicProfileDetailResult {
    data class Available(val profile: PublicProfileSummary) : PublicProfileDetailResult

    data object NotFound : PublicProfileDetailResult

    data object Unavailable : PublicProfileDetailResult
}
