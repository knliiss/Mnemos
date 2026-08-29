package com.mnemos.account.integration.keycloak.service

import com.mnemos.account.domain.model.user.User
import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.account.integration.keycloak.dto.KeycloakUserUpdateRequest
import org.springframework.stereotype.Service

@Service
class KeycloakUserIdentitySynchronizer(
    private val keycloakAdminClient: KeycloakAdminClient,
    private val keycloakAdminProperties: KeycloakAdminProperties,
    private val keycloakTokenProvider: KeycloakTokenProvider,
) {
    fun synchronize(user: User): User {
        val authorization = "Bearer ${keycloakTokenProvider.token()}"

        val keycloakUser =
            keycloakAdminClient.getUser(
                authorization = authorization,
                realm = keycloakAdminProperties.realm,
                userId = user.keycloakId.id,
            )

        val expectedUserId = user.id.id.toString()

        val currentUserId =
            keycloakUser.attributes
                ?.get(MNEMOS_USER_ID_ATTRIBUTE)
                ?.singleOrNull()

        if (currentUserId == expectedUserId) {
            return user
        }

        val attributes =
            keycloakUser.attributes
                .orEmpty()
                .toMutableMap()
                .apply {
                    this[MNEMOS_USER_ID_ATTRIBUTE] = listOf(expectedUserId)
                }

        keycloakAdminClient.updateUser(
            authorization = authorization,
            realm = keycloakAdminProperties.realm,
            userId = user.keycloakId.id,
            request =
                KeycloakUserUpdateRequest(
                    username = keycloakUser.username,
                    attributes = attributes,
                ),
        )

        return user
    }

    private companion object {
        const val MNEMOS_USER_ID_ATTRIBUTE = "mnemos_user_id"
    }
}
