package com.mnemos.accountservice.api.dto.user

import com.mnemos.accountservice.application.query.user.CurrentUserView
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
        id = id.value,
        username = username.value,
        localizationLanguage = localizationLanguage,
        activeAccountId = activeAccountId?.value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
