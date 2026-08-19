package com.mnemos.profile.application.profile.usecase.search

import com.mnemos.profile.application.common.pagination.PageResult
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchPublicProfilesService(
    private val repository: PublicProfileRepository,
) : SearchPublicProfilesUseCase {
    @Transactional(readOnly = true)
    override fun execute(query: SearchPublicProfilesQuery): PageResult<PublicProfile> {
        val normalizedQuery =
            query.copy(
                query = query.query.trim(),
            )

        require(normalizedQuery.query.isNotEmpty()) {
            "Profile search query cannot be blank"
        }

        return repository.search(normalizedQuery)
    }
}
