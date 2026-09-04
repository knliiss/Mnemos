package com.mnemos.account.integration.keycloak.service

import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.account.integration.keycloak.dto.KeycloakUserCreateRequest
import com.mnemos.account.integration.keycloak.dto.KeycloakUserResponse
import com.mnemos.account.integration.keycloak.dto.KeycloakUserUpdateRequest
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.user.Username
import feign.FeignException
import org.springframework.stereotype.Service

@Service
class TelegramKeycloakIdentityService(
    private val keycloakAdminClient: KeycloakAdminClient,
    private val keycloakAdminProperties: KeycloakAdminProperties,
    private val keycloakTokenProvider: KeycloakTokenProvider,
) {
    fun resolveOrCreate(
        telegramId: Long,
        username: Username,
        fallbackUsername: Username?,
    ): TelegramKeycloakIdentity {
        val authorization = authorization()
        val existingUser = findUser(telegramId, authorization)

        if (existingUser != null) {
            val synchronizedUser =
                synchronizeGeneratedUsername(
                    authorization = authorization,
                    telegramId = telegramId,
                    user = existingUser,
                    username = username,
                    fallbackUsername = fallbackUsername,
                )

            return synchronizedUser.toIdentity(telegramId)
        }

        return createIdentity(
            authorization = authorization,
            telegramId = telegramId,
            username = username,
            fallbackUsername = fallbackUsername,
        )
    }

    private fun createIdentity(
        authorization: String,
        telegramId: Long,
        username: Username,
        fallbackUsername: Username?,
    ): TelegramKeycloakIdentity {
        val candidates = usernameCandidates(username, fallbackUsername)

        for (candidate in candidates) {
            try {
                keycloakAdminClient.createUser(
                    authorization = authorization,
                    realm = keycloakAdminProperties.realm,
                    request =
                        KeycloakUserCreateRequest(
                            username = candidate.value,
                            attributes =
                                mapOf(
                                    TELEGRAM_USER_ID_ATTRIBUTE to listOf(telegramId.toString()),
                                ),
                        ),
                )
            } catch (exception: FeignException) {
                if (exception.status() != 409) {
                    throw exception
                }

                findUser(telegramId, authorization)?.let { concurrentUser ->
                    return concurrentUser.toIdentity(telegramId)
                }

                continue
            }

            val createdUser =
                findUser(telegramId, authorization)
                    ?: error("Keycloak Telegram identity was not found after bootstrap: $telegramId")

            return createdUser.toIdentity(telegramId)
        }

        error("No Keycloak username is available for Telegram identity: $telegramId")
    }

    private fun synchronizeGeneratedUsername(
        authorization: String,
        telegramId: Long,
        user: KeycloakUserResponse,
        username: Username,
        fallbackUsername: Username?,
    ): KeycloakUserResponse {
        if (!isGeneratedUsername(user.username, telegramId)) {
            return user
        }

        val userId =
            user.id
                ?: error("Keycloak Telegram identity has no user id: $telegramId")

        for (candidate in usernameCandidates(username, fallbackUsername)) {
            if (candidate.value == user.username) {
                return user
            }

            try {
                keycloakAdminClient.updateUser(
                    authorization = authorization,
                    realm = keycloakAdminProperties.realm,
                    userId = userId,
                    request =
                        KeycloakUserUpdateRequest(
                            username = candidate.value,
                            email = user.email,
                            attributes = user.attributes.orEmpty(),
                        ),
                )
            } catch (exception: FeignException) {
                if (exception.status() == 409) {
                    continue
                }

                throw exception
            }

            return user.copy(username = candidate.value)
        }

        error("No Keycloak username is available to replace generated Telegram identity: $telegramId")
    }

    private fun findUser(
        telegramId: Long,
        authorization: String,
    ): KeycloakUserResponse? =
        keycloakAdminClient
            .searchUsers(
                authorization = authorization,
                realm = keycloakAdminProperties.realm,
                query = "$TELEGRAM_USER_ID_ATTRIBUTE:$telegramId",
            ).firstOrNull { user ->
                user.attributes
                    ?.get(TELEGRAM_USER_ID_ATTRIBUTE)
                    ?.contains(telegramId.toString()) == true
            }

    private fun KeycloakUserResponse.toIdentity(telegramId: Long): TelegramKeycloakIdentity {
        val userId =
            id
                ?: error("Keycloak Telegram identity has no user id: $telegramId")

        return TelegramKeycloakIdentity(
            keycloakId = KeycloakId(userId),
            username = Username(username),
        )
    }

    private fun usernameCandidates(
        username: Username,
        fallbackUsername: Username?,
    ): List<Username> =
        listOfNotNull(username, fallbackUsername).distinct()

    private fun isGeneratedUsername(
        username: String,
        telegramId: Long,
    ): Boolean =
        username.equals("telegram_$telegramId", ignoreCase = true) ||
            username == telegramId.toString()

    private fun authorization(): String = "Bearer ${keycloakTokenProvider.token()}"

    private companion object {
        const val TELEGRAM_USER_ID_ATTRIBUTE = "telegram_user_id"
    }
}

data class TelegramKeycloakIdentity(
    val keycloakId: KeycloakId,
    val username: Username,
)
