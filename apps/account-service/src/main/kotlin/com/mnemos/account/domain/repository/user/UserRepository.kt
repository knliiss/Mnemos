package com.mnemos.account.domain.repository.user

import com.mnemos.account.domain.model.user.User
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.Username
import java.time.Instant

interface UserRepository {
    fun create(user: User): User

    fun update(user: User): User

    fun findByTelegramId(telegramId: TelegramId): User?

    fun findByKeycloakId(keycloakId: KeycloakId): User?

    fun existsByUsername(username: Username): Boolean

    fun existsByTelegramId(telegramId: TelegramId): Boolean

    fun clearActiveAccountByAccountId(
        accountId: AccountId,
        now: Instant,
    )

    fun clearActiveAccountIfMatches(
        userId: UserId,
        accountId: AccountId,
        updatedAt: Instant,
    ): Boolean

    fun createIfAbsentByKeycloakId(user: User): UserCreationResult
}
