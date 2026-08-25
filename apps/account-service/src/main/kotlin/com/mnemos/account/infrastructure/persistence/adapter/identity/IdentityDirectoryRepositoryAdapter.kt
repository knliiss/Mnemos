package com.mnemos.account.infrastructure.persistence.adapter.identity

import com.mnemos.account.application.query.identity.AccountIdentityDirectoryEntry
import com.mnemos.account.application.query.identity.IdentityDirectoryPage
import com.mnemos.account.application.query.identity.IdentityDirectoryQuery
import com.mnemos.account.application.query.identity.IdentityDirectoryRepository
import com.mnemos.account.application.query.identity.UserIdentityDirectoryEntry
import com.mnemos.account.infrastructure.persistence.adapter.identity.IdentityDirectoryPersistenceMapper.toDirectoryPage
import com.mnemos.account.infrastructure.persistence.adapter.identity.IdentityDirectoryPersistenceMapper.toEntry
import com.mnemos.account.infrastructure.persistence.repository.identity.JpaIdentitySearchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class IdentityDirectoryRepositoryAdapter(
    private val repository: JpaIdentitySearchRepository,
) : IdentityDirectoryRepository {
    override fun searchUsers(query: IdentityDirectoryQuery): IdentityDirectoryPage<UserIdentityDirectoryEntry> {
        val page =
            repository.searchUsersDirectory(
                query = query.query.orEmpty(),
                orderName = query.order.name,
                pageable = PageRequest.of(query.page, query.size),
            )

        return page.toDirectoryPage { projection -> projection.toEntry() }
    }

    override fun searchAccounts(query: IdentityDirectoryQuery): IdentityDirectoryPage<AccountIdentityDirectoryEntry> {
        val page =
            repository.searchAccountsDirectory(
                query = query.query.orEmpty(),
                orderName = query.order.name,
                pageable = PageRequest.of(query.page, query.size),
            )

        return page.toDirectoryPage { projection -> projection.toEntry() }
    }
}
