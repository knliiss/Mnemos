package com.mnemos.account.api.dto.user

import com.mnemos.account.application.query.user.CurrentUserView

object UserApiMapper {
    fun CurrentUserView.toResponse(): UserResponse =
        UserResponse(
            id = id.id,
            username = username.value,
            localizationLanguage = localizationLanguage,
            activeAccountId = activeAccountId?.id,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
