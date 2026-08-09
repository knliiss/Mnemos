package com.mnemos.account.api.dto.user

import com.mnemos.account.application.query.user.CurrentUserView
import com.mnemos.common.core.model.user.LocalizationLanguage
import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val localizationLanguage: LocalizationLanguage,
    val activeAccountId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun CurrentUserView.toResponse(): UserResponse =
    UserResponse(
        id = id.id,
        username = username.value,
        localizationLanguage = localizationLanguage,
        activeAccountId = activeAccountId?.id,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
