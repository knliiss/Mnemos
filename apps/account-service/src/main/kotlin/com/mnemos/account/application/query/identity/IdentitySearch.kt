package com.mnemos.account.application.query.identity

import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.util.UUID

data class IdentitySearchQuery(
    val query: String,
    val limit: Int,
)

data class IdentitySearchResult(
    val userId: UserId,
    val username: String,
    val telegramId: Long,
    val accounts: List<IdentityAccount>,
)

data class IdentityAccount(
    val accountId: AccountId,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val status: AccountStatus,
)

interface IdentitySearchRepository {
    fun search(query: IdentitySearchQuery): List<IdentitySearchResult>
}
