package com.mnemos.profile.application.profile.usecase.search

import com.mnemos.profile.application.common.pagination.PageQuery

data class SearchPublicProfilesQuery(
    val query: String,
    val page: PageQuery,
)
