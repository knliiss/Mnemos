package com.mnemos.profile.application.profile.usecase.search

import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class SearchPublicProfilesService(
    private val repository: PublicProfileRepository,
) : SearchPublicProfilesUseCase {
    @Transactional(readOnly = true)
    override fun execute(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile> {
        val normalized = query.trim()

        if (normalized.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile search query cannot be blank")
        }

        return repository.search(normalized, pageable)
    }
}
