package com.mnemos.account.infrastructure.persistence.adapter.identity

import com.mnemos.account.application.query.identity.AccountIdentityDirectoryEntry
import com.mnemos.account.application.query.identity.IdentityDirectoryPage
import com.mnemos.account.application.query.identity.UserIdentityDirectoryEntry
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.infrastructure.persistence.projection.identity.AccountIdentityDirectoryProjection
import com.mnemos.account.infrastructure.persistence.projection.identity.UserIdentityDirectoryProjection
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.data.domain.Page

object IdentityDirectoryPersistenceMapper {
    fun UserIdentityDirectoryProjection.toEntry(): UserIdentityDirectoryEntry =
        UserIdentityDirectoryEntry(
            userId = UserId(getUserId()),
            username = getUsername(),
            telegramId = getTelegramId(),
            activeAccountId = getActiveAccountId()?.let(::AccountId),
            accountCount = getAccountCount(),
            createdAt = getCreatedAt(),
        )

    fun AccountIdentityDirectoryProjection.toEntry(): AccountIdentityDirectoryEntry =
        AccountIdentityDirectoryEntry(
            accountId = AccountId(getAccountId()),
            cristalixUuid = getCristalixUuid(),
            cristalixNickname = getCristalixNickname(),
            status = AccountStatus.valueOf(getStatus()),
            ownerUserId = UserId(getOwnerUserId()),
            ownerUsername = getOwnerUsername(),
            createdAt = getCreatedAt(),
        )

    fun <T, R> Page<T>.toDirectoryPage(mapper: (T) -> R): IdentityDirectoryPage<R> =
        IdentityDirectoryPage(
            content = content.map(mapper),
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
}
