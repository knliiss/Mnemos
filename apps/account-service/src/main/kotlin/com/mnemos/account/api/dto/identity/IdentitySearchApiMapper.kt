package com.mnemos.account.api.dto.identity

import com.mnemos.account.application.query.identity.IdentityAccount
import com.mnemos.account.application.query.identity.IdentitySearchQuery
import com.mnemos.account.application.query.identity.IdentitySearchResult

object IdentitySearchApiMapper {
    fun IdentitySearchRequest.toQuery(): IdentitySearchQuery =
        IdentitySearchQuery(
            query = query,
            limit = limit,
        )

    fun IdentitySearchResult.toResponse(): IdentitySearchResponse =
        IdentitySearchResponse(
            userId = userId.id,
            username = username,
            telegramId = telegramId,
            accounts = accounts.map { account -> account.toResponse() },
        )

    private fun IdentityAccount.toResponse(): IdentityAccountResponse =
        IdentityAccountResponse(
            accountId = accountId.id,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            status = status.name,
        )
}
