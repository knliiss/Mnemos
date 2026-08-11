package com.mnemos.account.domain.model.user

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import java.time.Instant

class User private constructor(
    val id: UserId,
    val telegramId: TelegramId,
    val keycloakId: KeycloakId,
    username: Username,
    localizationLanguage: LocalizationLanguage,
    val createdAt: Instant,
    updatedAt: Instant,
    activeAccountId: AccountId?,
) {
    var username: Username = username
        private set
    var updatedAt: Instant = updatedAt
        private set
    var localizationLanguage: LocalizationLanguage = localizationLanguage
        private set
    var activeAccountId: AccountId? = activeAccountId
        private set

    companion object {
        fun create(
            telegramId: TelegramId,
            username: Username,
            keycloakId: KeycloakId,
        ): User {
            val now = Instant.now()

            return User(
                id = UserId.generate(),
                telegramId = telegramId,
                username = username,
                keycloakId = keycloakId,
                localizationLanguage = LocalizationLanguage.EN,
                createdAt = now,
                updatedAt = now,
                activeAccountId = null,
            )
        }

        fun restore(
            id: UserId,
            telegramId: TelegramId,
            username: Username,
            keycloakId: KeycloakId,
            localizationLanguage: LocalizationLanguage,
            createdAt: Instant,
            updatedAt: Instant,
            activeAccountId: AccountId?,
        ): User =
            User(
                id = id,
                telegramId = telegramId,
                username = username,
                keycloakId = keycloakId,
                localizationLanguage = localizationLanguage,
                createdAt = createdAt,
                updatedAt = updatedAt,
                activeAccountId = activeAccountId,
            )
    }

    fun updateUsername(
        user: User,
        newUsername: Username,
    ) {
        if (user.username == newUsername) return
        user.username = newUsername
        user.updatedAt = Instant.now()
    }

    fun updateLocalization(
        user: User,
        localizationLanguage: LocalizationLanguage,
    ) {
        if (user.localizationLanguage == localizationLanguage) return
        user.localizationLanguage = localizationLanguage
        user.updatedAt = Instant.now()
    }

    fun changeActiveAccount(accountId: AccountId): Boolean {
        if (activeAccountId == accountId) return false
        activeAccountId = accountId
        updatedAt = Instant.now()
        return true
    }

    fun clearActiveAccount(): Boolean {
        if (activeAccountId == null) return false
        updatedAt = Instant.now()
        activeAccountId = null
        return true
    }

    override fun toString(): String {
        val s = String.format("User [id=%s],[username=%s]", id, username.value)
        return s
    }
}
