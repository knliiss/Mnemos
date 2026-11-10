package com.mnemos.contracts.profile.internal.v1

data class InternalTelegramProfileSearchPage(
    val items: List<InternalTelegramPublicProfileSummary>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)
