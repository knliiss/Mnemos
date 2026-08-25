package com.mnemos.account.application.service.identity

import com.mnemos.account.application.query.identity.AccountIdentityDirectoryEntry
import com.mnemos.account.application.query.identity.IdentityDirectoryPage
import com.mnemos.account.application.query.identity.IdentityDirectoryQuery
import com.mnemos.account.application.query.identity.IdentityDirectoryRepository
import com.mnemos.account.application.query.identity.UserIdentityDirectoryEntry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class IdentityDirectoryService(
    private val identityDirectoryRepository: IdentityDirectoryRepository,
) {
    fun searchUsers(query: IdentityDirectoryQuery): IdentityDirectoryPage<UserIdentityDirectoryEntry> =
        identityDirectoryRepository.searchUsers(normalize(query))

    fun searchAccounts(query: IdentityDirectoryQuery): IdentityDirectoryPage<AccountIdentityDirectoryEntry> =
        identityDirectoryRepository.searchAccounts(normalize(query))

    private fun normalize(query: IdentityDirectoryQuery): IdentityDirectoryQuery {
        val normalizedQuery = query.query?.trim()?.takeIf { value -> value.isNotEmpty() }

        require(query.page >= 0) {
            "Identity directory page must be >= 0"
        }
        require(query.size in MIN_SIZE..MAX_SIZE) {
            "Identity directory size must be between $MIN_SIZE and $MAX_SIZE"
        }
        require(normalizedQuery == null || normalizedQuery.length <= MAX_QUERY_LENGTH) {
            "Identity directory query must contain at most $MAX_QUERY_LENGTH characters"
        }

        return query.copy(query = normalizedQuery)
    }

    private companion object {
        const val MIN_SIZE = 1
        const val MAX_SIZE = 50
        const val MAX_QUERY_LENGTH = 64
    }
}
