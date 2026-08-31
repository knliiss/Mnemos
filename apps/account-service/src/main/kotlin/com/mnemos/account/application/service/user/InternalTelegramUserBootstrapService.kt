package com.mnemos.account.application.service.user

import com.mnemos.account.application.service.user.UserIntegrationEvents.created
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
    private val telegramKeycloakIdentityService: TelegramKeycloakIdentityService,
    private val keycloakUserIdentitySynchronizer: KeycloakUserIdentitySynchronizer,
    private val publisher: IntegrationEventPublisher,
) {
    @Transactional
    fun getOrCreate(telegramId: Long): User {
        val domainTelegramId = TelegramId(telegramId)

        userRepository.findByTelegramId(domainTelegramId)?.let { user -> return user }

        val keycloakId = telegramKeycloakIdentityService.resolveOrCreate(telegramId)
        val existingByKeycloak = userRepository.findByKeycloakId(keycloakId)

        if (existingByKeycloak != null) {
            return existingByKeycloak
        }

        val creation =
            userRepository.createIfAbsentByKeycloakId(
                User.create(
                    telegramId = domainTelegramId,
                    username = Username("telegram_$telegramId"),
                    keycloakId = keycloakId,
                ),
            )
        val synchronizedUser = keycloakUserIdentitySynchronizer.synchronize(creation.user)

        publishCreatedUser(creation, synchronizedUser)

        return synchronizedUser
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
