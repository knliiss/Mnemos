package com.mnemos.account.api.dto.user

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
