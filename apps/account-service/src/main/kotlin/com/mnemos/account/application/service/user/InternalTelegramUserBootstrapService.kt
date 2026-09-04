package com.mnemos.account.application.service.user

import com.mnemos.account.application.service.user.UserIntegrationEvents.created
import com.mnemos.account.domain.exception.user.UserIdentityConflictException
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserCreationResult
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.integration.keycloak.service.KeycloakUserIdentitySynchronizer
import com.mnemos.account.integration.keycloak.service.TelegramKeycloakIdentityService
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InternalTelegramUserBootstrapService(
    private val userRepository: UserRepository,
    private val usernameResolver: TelegramUsernameResolver,
    private val telegramKeycloakIdentityService: TelegramKeycloakIdentityService,
    private val keycloakUserIdentitySynchronizer: KeycloakUserIdentitySynchronizer,
    private val publisher: IntegrationEventPublisher,
) {
    @Transactional
    fun getOrCreate(
        telegramId: Long,
        telegramUsername: String? = null,
    ): User {
        val domainTelegramId = TelegramId(telegramId)
        val existingUser = userRepository.findByTelegramId(domainTelegramId)

        if (existingUser != null) {
            return synchronizeGeneratedUsername(
                user = existingUser,
                telegramUsername = telegramUsername,
            )
        }

        val resolution = usernameResolver.resolve(domainTelegramId, telegramUsername)
        val keycloakIdentity =
            telegramKeycloakIdentityService.resolveOrCreate(
                telegramId = telegramId,
                username = resolution.selected,
                fallbackUsername = resolution.keycloakFallback,
            )
        val existingByKeycloak = userRepository.findByKeycloakId(keycloakIdentity.keycloakId)

        if (existingByKeycloak != null) {
            return synchronizeGeneratedUsername(
                user = existingByKeycloak,
                telegramUsername = telegramUsername,
            )
        }

        ensureUsernameAvailable(
            username = keycloakIdentity.username,
            expectedUsername = resolution.selected,
        )

        val creation =
            userRepository.createIfAbsentByKeycloakId(
                User.create(
                    telegramId = domainTelegramId,
                    username = keycloakIdentity.username,
                    keycloakId = keycloakIdentity.keycloakId,
                ),
            )
        val synchronizedUser = keycloakUserIdentitySynchronizer.synchronize(creation.user)

        publishCreatedUser(creation, synchronizedUser)

        return synchronizedUser
    }

    private fun synchronizeGeneratedUsername(
        user: User,
        telegramUsername: String?,
    ): User {
        if (!shouldSynchronizeGeneratedUsername(user, telegramUsername)) {
            return user
        }

        val resolution = usernameResolver.resolve(user.telegramId, telegramUsername)
        val keycloakIdentity =
            telegramKeycloakIdentityService.resolveOrCreate(
                telegramId = user.telegramId.id,
                username = resolution.selected,
                fallbackUsername = resolution.keycloakFallback,
            )
        val resolvedUsername = keycloakIdentity.username

        ensureUsernameAvailable(
            username = resolvedUsername,
            expectedUsername = resolution.selected,
        )

        if (user.username == resolvedUsername) {
            return user
        }

        user.updateUsername(
            user = user,
            newUsername = resolvedUsername,
        )

        return userRepository.update(user)
    }

    private fun shouldSynchronizeGeneratedUsername(
        user: User,
        telegramUsername: String?,
    ): Boolean {
        val telegramId = user.telegramId.id
        val currentUsername = user.username.value

        if (currentUsername.equals("telegram_$telegramId", ignoreCase = true)) {
            return true
        }

        return currentUsername == telegramId.toString() && !telegramUsername.isNullOrBlank()
    }

    private fun ensureUsernameAvailable(
        username: Username,
        expectedUsername: Username,
    ) {
        if (username == expectedUsername || !userRepository.existsByUsername(username)) {
            return
        }

        throw UserIdentityConflictException(
            message = "Keycloak resolved a username already used by another Mnemos user",
            details = mapOf("username" to username.value),
        )
    }

    private fun publishCreatedUser(
        creation: UserCreationResult,
        user: User,
    ) {
        if (!creation.created) {
            return
        }

        publisher.publish(
            user.created(),
            metadata = EventMetadata(actorUserId = user.id.id),
        )
    }
}
