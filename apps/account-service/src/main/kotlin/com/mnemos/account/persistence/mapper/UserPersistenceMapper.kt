package com.mnemos.account.persistence.mapper

import com.mnemos.account.domain.model.user.User
import com.mnemos.account.persistence.entity.user.UserEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.Username

object UserPersistenceMapper {
    fun User.toEntity() =
        UserEntity(
            id = id.id,
            telegramId = telegramId.value,
            username = username.value,
            keycloakId = keycloakId.id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            localizationLanguage = localizationLanguage,
            activeAccountId = activeAccountId?.id,
        )

    fun UserEntity.toDomain() =
        User.restore(
            id = UserId(id),
            telegramId = TelegramId(telegramId),
            username = Username(username),
            keycloakId = KeycloakId(keycloakId),
            createdAt = createdAt,
            updatedAt = updatedAt,
            localizationLanguage = localizationLanguage,
            activeAccountId = activeAccountId?.let { AccountId(it) },
        )

    fun UserEntity.updateFrom(user: User) {
        username = user.username.value
        localizationLanguage = user.localizationLanguage
        updatedAt = user.updatedAt
        activeAccountId = user.activeAccountId?.id
    }
}
