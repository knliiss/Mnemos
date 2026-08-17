package com.mnemos.account.infrastructure.persistence.adapter.identity

import com.mnemos.account.application.query.identity.IdentityAccount
import com.mnemos.account.application.query.identity.IdentitySearchRepository
import com.mnemos.account.application.query.identity.IdentitySearchResult
import com.mnemos.account.infrastructure.persistence.projection.identity.IdentitySearchProjection
import com.mnemos.account.infrastructure.persistence.repository.identity.JpaIdentitySearchRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Repository

@Repository
class IdentitySearchRepositoryAdapter(
    private val repository: JpaIdentitySearchRepository,
) : IdentitySearchRepository {
    override fun search(
        query: String,
        limit: Int,
    ): List<IdentitySearchResult> =
        repository
            .search(
                query = query,
                limit = limit,
            ).groupBy { projection -> projection.getUserId() }
            .values
            .map { rows -> rows.toResult() }

    private fun List<IdentitySearchProjection>.toResult(): IdentitySearchResult {
        val user = first()
        val accounts = mapNotNull { row -> row.toAccount() }

        return IdentitySearchResult(
            userId = UserId(user.getUserId()),
            username = user.getUsername(),
            telegramId = user.getTelegramId(),
            accounts = accounts,
        )
    }

    private fun IdentitySearchProjection.toAccount(): IdentityAccount? {
        val accountId = getAccountId() ?: return null
        val cristalixUuid = getCristalixUuid() ?: return null
        val cristalixNickname = getCristalixNickname() ?: return null

        return IdentityAccount(
            accountId = AccountId(accountId),
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            active = getActive() == true,
        )
    }
}
