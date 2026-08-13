package com.mnemos.moderation.adapter.out.keycloak

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.application.security.port.out.UserRoleProvider
import org.springframework.stereotype.Component

@Component
class KeycloakUserRoleProvider(
    private val client: KeycloakAdminClient,
    private val properties: KeycloakAdminProperties,
    private val tokenProvider: KeycloakTokenProvider,
) : UserRoleProvider {
    override fun highestRole(userId: UserId): UserRole {
        val authorization = "Bearer ${tokenProvider.token()}"
        val expectedUserId = userId.id.toString()
        val users =
            client.findUsers(
                authorization = authorization,
                realm = properties.realm,
                query = "$MNEMOS_USER_ID_ATTRIBUTE:$expectedUserId",
            )

        val user =
            users.firstOrNull { candidate ->
                candidate.attributes[MNEMOS_USER_ID_ATTRIBUTE]?.contains(expectedUserId) == true
            } ?: return UserRole.USER

        val roles =
            client
                .getEffectiveRealmRoles(
                    authorization = authorization,
                    realm = properties.realm,
                    keycloakUserId = user.id,
                ).map { role -> role.name.uppercase() }
                .toSet()

        return when {
            UserRole.SUPER_ADMIN.name in roles -> UserRole.SUPER_ADMIN
            UserRole.ADMIN.name in roles -> UserRole.ADMIN
            UserRole.MODERATOR.name in roles -> UserRole.MODERATOR
            else -> UserRole.USER
        }
    }

    private companion object {
        const val MNEMOS_USER_ID_ATTRIBUTE = "mnemos_user_id"
    }
}
