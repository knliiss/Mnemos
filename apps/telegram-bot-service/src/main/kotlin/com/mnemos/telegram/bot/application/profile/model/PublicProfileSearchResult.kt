package com.mnemos.telegram.bot.application.profile.model

sealed interface PublicProfileSearchResult {
    data class Available(val page: PublicProfileSearchPage) : PublicProfileSearchResult

    data object Unavailable : PublicProfileSearchResult
}
