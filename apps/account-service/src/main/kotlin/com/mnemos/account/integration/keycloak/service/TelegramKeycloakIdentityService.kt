package com.mnemos.account.integration.keycloak.service

import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.account.integration.keycloak.dto.KeycloakUserCreateRequest
import com.mnemos.common.core.model.id.KeycloakId
import feign.FeignException
import org.springframework.stereotype.Service

@Service
class TelegramKeycloakIdentityService(
    private val keycloakAdminClient: KeycloakAdminClient,
    private val keycloakAdminProperties: KeycloakAdminProperties,
    private val keycloakTokenProvider: KeycloakTokenProvider,
) {
    fun resolveOrCreate(telegramId: Long): KeycloakId {
        findUserId(telegramId)?.let { userId -> return KeycloakId(userId) }

        val authorization = authorization()

        try {
            keycloakAdminClient.createUser(
                authorization = authorization,
                realm = keycloakAdminProperties.realm,
                request =
                    KeycloakUserCreateRequest(
                        username = "telegram_$telegramId",
                        attributes = mapOf(TELEGRAM_USER_ID_ATTRIBUTE to listOf(telegramId.toString())),
                    ),
            )
        } catch (exception: FeignException) {
            if (exception.status() != 409) {
                throw exception
            }
        }

        val userId =
            findUserId(telegramId)
                ?: error("Keycloak Telegram identity was not found after bootstrap: $telegramId")

        return KeycloakId(userId)
    }

    private fun findUserId(telegramId: Long) =
        keycloakAdminClient
            .searchUsers(
                authorization = authorization(),
                realm = keycloakAdminProperties.realm,
                query = "$TELEGRAM_USER_ID_ATTRIBUTE:$telegramId",
            )
            .firstOrNull { user ->
                user.attributes
                    ?.get(TELEGRAM_USER_ID_ATTRIBUTE)
                    ?.contains(telegramId.toString()) == true
            }
            ?.id

    private fun authorization(): String = "Bearer ${keycloakTokenProvider.token()}"

    private companion object {
        const val TELEGRAM_USER_ID_ATTRIBUTE = "telegram_user_id"
    }
}
