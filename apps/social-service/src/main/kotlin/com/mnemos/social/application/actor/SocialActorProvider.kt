package com.mnemos.social.application.actor

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Component
class SocialActorProvider(
    private val currentMnemosUser: CurrentMnemosUser,
    private val accountAccessPort: AccountAccessPort,
) {
    fun requireActor(permission: RolePermission? = null): SocialActor {
        val userId = currentMnemosUser.requireUserId()
        val actor = accountAccessPort.resolveActiveActor(userId)

        if (
            permission != null &&
            !accountAccessPort.hasPermission(
                accountId = actor.accountId,
                userId = actor.userId,
                permission = permission,
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account permission is required: $permission",
            )
        }

        return actor
    }

    fun requireUserId(): UUID = currentMnemosUser.requireUserId()

    fun hasAnyUserRole(vararg roles: UserRole): Boolean {
        val currentRoles = currentMnemosUser.get().roles

        return roles.any(currentRoles::contains)
    }

    fun hasPermission(
        accountId: UUID,
        userId: UUID,
        permission: RolePermission,
    ): Boolean =
        accountAccessPort.hasPermission(
            accountId = accountId,
            userId = userId,
            permission = permission,
        )
}
