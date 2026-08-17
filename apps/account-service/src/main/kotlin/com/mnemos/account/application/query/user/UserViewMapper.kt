package com.mnemos.account.application.query.user

import com.mnemos.account.domain.model.user.User

object UserViewMapper {
    fun User.toCurrentUserView(): CurrentUserView =
        CurrentUserView(
            id = id,
            username = username,
            localizationLanguage = localizationLanguage,
            activeAccountId = activeAccountId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
