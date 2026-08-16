package com.mnemos.profile.application.profile.usecase.search

import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SearchPublicProfilesUseCase {
    fun execute(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile>
}
