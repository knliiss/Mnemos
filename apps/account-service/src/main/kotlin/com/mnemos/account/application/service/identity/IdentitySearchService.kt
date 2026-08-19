package com.mnemos.account.application.service.identity

import com.mnemos.account.application.query.identity.IdentitySearchQuery
import com.mnemos.account.application.query.identity.IdentitySearchRepository
import com.mnemos.account.application.query.identity.IdentitySearchResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class IdentitySearchService(
    private val identitySearchRepository: IdentitySearchRepository,
) {
    fun search(query: IdentitySearchQuery): List<IdentitySearchResult> {
        val normalizedQuery =
            query.copy(
                query = query.query.trim(),
                limit = query.limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
            )

        require(normalizedQuery.query.length >= MIN_QUERY_LENGTH) {
            "Identity search query must contain at least $MIN_QUERY_LENGTH characters"
        }

        return identitySearchRepository.search(normalizedQuery)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 50
    }
}
