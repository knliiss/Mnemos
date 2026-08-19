package com.mnemos.account.infrastructure.persistence.adapter.identity

import com.mnemos.account.application.query.identity.IdentityAccount
import com.mnemos.account.application.query.identity.IdentitySearchResult
import com.mnemos.account.infrastructure.persistence.projection.identity.IdentitySearchProjection
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

object IdentitySearchPersistenceMapper {
    fun List<IdentitySearchProjection>.toResult(): IdentitySearchResult {
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
