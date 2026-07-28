package com.mnemos.accountservice.persistence.mapper

import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.persistence.entity.user.UserEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.core.model.user.Username

object UserPersistenceMapper {
    fun User.toEntity() =
        UserEntity(
            id = id.value,
            telegramId = telegramId.value,
            username = username.value,
            keycloakId = keycloakId.value,
            createdAt = createdAt,
            updatedAt = updatedAt,
            localizationLanguage = localizationLanguage,
            activeAccountId = activeAccountId?.value,
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
        activeAccountId = user.activeAccountId?.value
    }
}
