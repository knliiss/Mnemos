package com.mnemos.accountservice.api.mapper.user

import com.mnemos.accountservice.api.dto.user.UserResponse
import com.mnemos.accountservice.domain.model.user.User

object UserMapper {
    fun User.toResponse() = UserResponse(
        id = this.id.value,
        telegramId = this.telegramId.value,
        username = this.username.value,
        keycloakId = this.keycloakId.value,
        localizationLanguage = this.localizationLanguage,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        activeAccountId = this.activeAccountId?.value
    )
}