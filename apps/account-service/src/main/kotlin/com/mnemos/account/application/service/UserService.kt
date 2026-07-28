package com.mnemos.account.application.service

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.application.query.user.CurrentUserView
import com.mnemos.account.application.query.user.toCurrentUserView
import com.mnemos.account.domain.exception.UserIdentityConflictException
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val currentMnemosUser: CurrentMnemosUser,
    private val keycloakAdminClient: KeycloakAdminClient,
    private val keycloakAdminProperties: KeycloakAdminProperties,
    private val keycloakTokenProvider: KeycloakTokenProvider,
) {
    @Transactional
    fun initializeCurrentUser(): CurrentUserView {
        val principal = currentMnemosUser.get()
        val keycloakId = KeycloakId(principal.keycloakId)

        userRepository.findByKeycloakId(keycloakId)?.let {
            return it.toCurrentUserView()
        }

        val telegramId = TelegramId(principal.telegramId)
        val username = Username(principal.username)

        validateIdentityAvailability(
            telegramId = telegramId,
            username = username,
        )

        val user =
            User.create(
                telegramId = telegramId,
                username = username,
                keycloakId = keycloakId,
            )

        return userRepository.create(user).toCurrentUserView()
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(): CurrentUserView = currentUserProvider.get().toCurrentUserView()

    @Transactional
    fun updateUsername(newUsername: Username): CurrentUserView {
        val user = currentUserProvider.get()

        if (user.username == newUsername) {
            return user.toCurrentUserView()
        }

        if (userRepository.existsByUsername(newUsername)) {
            throw UserIdentityConflictException(
                message = "Username is already used by another user",
                details = mapOf("username" to newUsername.value),
            )
        }

        val realm = keycloakAdminProperties.realm
        val keycloakUserId = user.keycloakId.value
        val authorization = "Bearer ${keycloakTokenProvider.token()}"

        user.updateUsername(user, newUsername)
        val updatedUser = userRepository.update(user)

        val keycloakUser = keycloakAdminClient.getUser(
            authorization = authorization,
            realm = realm,
            userId = keycloakUserId
        )

        keycloakAdminClient.updateUser(
            authorization = authorization,
            realm = realm,
            userId = keycloakUserId,
            request = keycloakUser.copy(username = newUsername.value)
        )

        return updatedUser.toCurrentUserView()
    }

    @Transactional
    fun updateLocalization(localizationLanguage: LocalizationLanguage): CurrentUserView {
        val user = currentUserProvider.get()

        if (user.localizationLanguage == localizationLanguage) {
            return user.toCurrentUserView()
        }

        user.updateLocalization(user, localizationLanguage)
        return userRepository.update(user).toCurrentUserView()
    }

    private fun validateIdentityAvailability(
        telegramId: TelegramId,
        username: Username,
    ) {
        if (userRepository.existsByTelegramId(telegramId)) {
            throw UserIdentityConflictException(
                message = "Telegram account is already linked to another user",
                details = mapOf("telegramId" to telegramId.value),
            )
        }

        if (userRepository.existsByUsername(username)) {
            throw UserIdentityConflictException(
                message = "Username is already used by another user",
                details = mapOf("username" to username.value),
            )
        }
    }
}
