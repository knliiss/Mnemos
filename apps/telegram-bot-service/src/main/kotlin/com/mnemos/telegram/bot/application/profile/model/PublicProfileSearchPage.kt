package com.mnemos.telegram.bot.application.profile.model

data class PublicProfileSearchPage(
    val items: List<PublicProfileSummary>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
)
