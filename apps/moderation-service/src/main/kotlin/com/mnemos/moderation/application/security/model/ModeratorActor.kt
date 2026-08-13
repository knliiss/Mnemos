package com.mnemos.moderation.application.security.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole

data class ModeratorActor(
    val userId: UserId,
    val role: UserRole,
)
