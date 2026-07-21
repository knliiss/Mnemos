package com.mnemos.accountservice.application.service

import com.mnemos.accountservice.api.dto.user.UserResponse
import com.mnemos.accountservice.api.mapper.user.UserMapper.toResponse
import com.mnemos.accountservice.application.security.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.UserIdentityConflictException
import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.accountservice.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.accountservice.integration.keycloak.config.KeycloakAdminProperties
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
    private val keycloakAdminProperties: KeycloakAdminProperties
) {
    @Transactional
    fun initializeCurrentUser(): UserResponse {
        val principal = currentMnemosUser.get()
        val keycloakId = KeycloakId(principal.keycloakId)

        userRepository.findByKeycloakId(keycloakId)?.let {
            return it.toResponse()
        }

        val telegramId = TelegramId(principal.telegramId)
        val username = Username(principal.username)

        validateIdentityAvailability(
            telegramId = telegramId, username = username
        )

        val user = User.create(
            telegramId = telegramId, username = username, keycloakId = keycloakId
        )

        return userRepository.create(user).toResponse()
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(): UserResponse {
        return currentUserProvider.get().toResponse()
    }

    @Transactional
    fun updateUsername(newUsername: Username): UserResponse {
        val user = currentUserProvider.get()

        if (user.username == newUsername) {
            return user.toResponse()
        }

        if (userRepository.existsByUsername(newUsername)) {
            throw UserIdentityConflictException(
                message = "Username is already used by another user", details = mapOf("username" to newUsername.value)
            )
        }

        val realm = keycloakAdminProperties.realm
        val keycloakUserId = user.keycloakId.value

        user.updateUsername(user, newUsername)
        val updatedUser = userRepository.update(user)
        val keycloakUser = keycloakAdminClient.getUser(
            realm = realm, userId = keycloakUserId
        )

        keycloakAdminClient.updateUser(
            realm = realm, userId = keycloakUserId, request = keycloakUser.copy(username = newUsername.value)
        )

        return updatedUser.toResponse()
    }

    @Transactional
    fun updateLocalization(localizationLanguage: LocalizationLanguage): UserResponse {
        val user = currentUserProvider.get()

        if (user.localizationLanguage == localizationLanguage) {
            return user.toResponse()
        }

        user.updateLocalization(user, localizationLanguage)
        return userRepository.update(user).toResponse()
    }

    private fun validateIdentityAvailability(
        telegramId: TelegramId, username: Username
    ) {
        if (userRepository.existsByTelegramId(telegramId)) {
            throw UserIdentityConflictException(
                message = "Telegram account is already linked to another user",
                details = mapOf("telegramId" to telegramId.value)
            )
        }

        if (userRepository.existsByUsername(username)) {
            throw UserIdentityConflictException(
                message = "Username is already used by another user", details = mapOf("username" to username.value)
            )
        }
    }


}