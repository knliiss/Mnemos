package com.mnemos.account.infrastructure.persistence.adapter.identity

import com.mnemos.account.application.query.identity.IdentitySearchQuery
import com.mnemos.account.application.query.identity.IdentitySearchRepository
import com.mnemos.account.application.query.identity.IdentitySearchResult
import com.mnemos.account.infrastructure.persistence.adapter.identity.IdentitySearchPersistenceMapper.toResult
import com.mnemos.account.infrastructure.persistence.repository.identity.JpaIdentitySearchRepository
import org.springframework.stereotype.Repository

@Repository
class IdentitySearchRepositoryAdapter(
    private val repository: JpaIdentitySearchRepository,
) : IdentitySearchRepository {
    override fun search(query: IdentitySearchQuery): List<IdentitySearchResult> =
        repository
            .search(
                query = query.query,
                limit = query.limit,
            ).groupBy { projection -> projection.getUserId() }
            .values
            .map { rows -> rows.toResult() }
}
