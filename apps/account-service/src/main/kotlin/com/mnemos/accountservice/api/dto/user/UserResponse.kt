package com.mnemos.accountservice.api.dto.user

import com.mnemos.common.core.model.user.LocalizationLanguage
import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val telegramId: Long,
    val username: String,
    val keycloakId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val activeAccountId: UUID?,

    val localizationLanguage: LocalizationLanguage,
)
