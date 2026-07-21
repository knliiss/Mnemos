package com.mnemos.security

import com.mnemos.common.core.model.user.UserRole
import java.util.UUID

data class MnemosPrincipal(
    val keycloakId: UUID,
    val username: String,
    val telegramId: Long,
    val roles: Set<UserRole>
)