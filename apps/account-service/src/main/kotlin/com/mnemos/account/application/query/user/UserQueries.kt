package com.mnemos.account.application.query.user

import com.mnemos.account.domain.model.user.User
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import java.time.Instant

data class CurrentUserView(
    val id: UserId,
    val username: Username,
    val localizationLanguage: LocalizationLanguage,
    val activeAccountId: AccountId?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun User.toCurrentUserView(): CurrentUserView =
    CurrentUserView(
        id = id,
        username = username,
        localizationLanguage = localizationLanguage,
        activeAccountId = activeAccountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
