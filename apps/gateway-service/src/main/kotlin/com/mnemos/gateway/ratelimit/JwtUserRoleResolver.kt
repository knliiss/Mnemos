package com.mnemos.gateway.ratelimit

import com.mnemos.common.core.model.user.UserRole
import org.springframework.stereotype.Component

@Component
class JwtUserRoleResolver {
    fun resolve(realmAccessClaim: Any?): UserRole {
        val realmAccess = realmAccessClaim as? Map<*, *> ?: return UserRole.USER
        val roles = realmAccess[ROLES_CLAIM] as? Collection<*> ?: return UserRole.USER
        val roleNames = roles.mapNotNull { role -> role?.toString()?.uppercase() }.toSet()

        return when {
            UserRole.SUPER_ADMIN.name in roleNames -> UserRole.SUPER_ADMIN
            UserRole.ADMIN.name in roleNames -> UserRole.ADMIN
            UserRole.MODERATOR.name in roleNames -> UserRole.MODERATOR
            else -> UserRole.USER
        }
    }

    private companion object {
        const val ROLES_CLAIM = "roles"
    }
}
