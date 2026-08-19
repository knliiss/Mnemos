package com.mnemos.profile.application.profile.usecase.search

import com.mnemos.profile.application.common.pagination.PageResult
import com.mnemos.profile.domain.profile.model.PublicProfile

interface SearchPublicProfilesUseCase {
    fun execute(query: SearchPublicProfilesQuery): PageResult<PublicProfile>
}
