package com.mnemos.account.api.dto.identity

import com.mnemos.account.application.query.identity.AccountIdentityDirectoryEntry
import com.mnemos.account.application.query.identity.IdentityDirectoryPage
import com.mnemos.account.application.query.identity.IdentityDirectoryQuery
import com.mnemos.account.application.query.identity.UserIdentityDirectoryEntry

object IdentityDirectoryApiMapper {
    fun IdentityDirectoryRequest.toQuery(): IdentityDirectoryQuery =
        IdentityDirectoryQuery(
            query = query,
            page = page,
            size = size,
            order = order,
        )

    fun IdentityDirectoryPage<UserIdentityDirectoryEntry>.toUserResponse():
        IdentityDirectoryPageResponse<UserIdentityDirectoryResponse> =
        IdentityDirectoryPageResponse(
            content =
                content.map { entry ->
                    UserIdentityDirectoryResponse(
                        userId = entry.userId.id,
                        username = entry.username,
                        telegramId = entry.telegramId,
                        activeAccountId = entry.activeAccountId?.id,
                        accountCount = entry.accountCount,
                        createdAt = entry.createdAt,
                    )
                },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )

    fun IdentityDirectoryPage<AccountIdentityDirectoryEntry>.toAccountResponse():
        IdentityDirectoryPageResponse<AccountIdentityDirectoryResponse> =
        IdentityDirectoryPageResponse(
            content =
                content.map { entry ->
                    AccountIdentityDirectoryResponse(
                        accountId = entry.accountId.id,
                        cristalixUuid = entry.cristalixUuid,
                        cristalixNickname = entry.cristalixNickname,
                        status = entry.status,
                        ownerUserId = entry.ownerUserId.id,
                        ownerUsername = entry.ownerUsername,
                        createdAt = entry.createdAt,
                    )
                },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
}
