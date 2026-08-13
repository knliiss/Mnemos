package com.mnemos.moderation.application.security.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole

interface UserRoleProvider {
    fun highestRole(userId: UserId): UserRole
}
