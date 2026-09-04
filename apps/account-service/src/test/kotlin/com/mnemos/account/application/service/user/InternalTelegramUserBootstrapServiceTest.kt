package com.mnemos.account.application.service.user

import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserCreationResult
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.integration.keycloak.service.KeycloakUserIdentitySynchronizer
import com.mnemos.account.integration.keycloak.service.TelegramKeycloakIdentity
import com.mnemos.account.integration.keycloak.service.TelegramKeycloakIdentityService
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class InternalTelegramUserBootstrapServiceTest {
    private val userRepository = mock<UserRepository>()
    private val usernameResolver = mock<TelegramUsernameResolver>()
    private val keycloakIdentityService = mock<TelegramKeycloakIdentityService>()
    private val keycloakIdentitySynchronizer = mock<KeycloakUserIdentitySynchronizer>()
    private val publisher = mock<IntegrationEventPublisher>()
    private val service =
        InternalTelegramUserBootstrapService(
            userRepository = userRepository,
            usernameResolver = usernameResolver,
            telegramKeycloakIdentityService = keycloakIdentityService,
            keycloakUserIdentitySynchronizer = keycloakIdentitySynchronizer,
            publisher = publisher,
        )

    @Test
    fun `new bot user is created with Telegram username`() {
        val telegramId = TelegramId(910484582)
        val username = Username("knalis")
        val fallback = Username("knalis_910484582")
        val keycloakId = KeycloakId(UUID.fromString("632cf054-ac8a-459a-b7af-46f3100cfa67"))

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(null)
        whenever(usernameResolver.resolve(telegramId, "knalis"))
            .thenReturn(TelegramUsernameResolution(username, fallback))
        whenever(keycloakIdentityService.resolveOrCreate(telegramId.id, username, fallback))
            .thenReturn(TelegramKeycloakIdentity(keycloakId, username))
        whenever(userRepository.findByKeycloakId(keycloakId)).thenReturn(null)
        whenever(userRepository.createIfAbsentByKeycloakId(any())).thenAnswer { invocation ->
            UserCreationResult(
                user = invocation.getArgument(0),
                created = true,
            )
        }
        whenever(keycloakIdentitySynchronizer.synchronize(any())).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val user = service.getOrCreate(telegramId.id, "knalis")

        assertEquals(username, user.username)
        assertEquals(telegramId, user.telegramId)
        assertEquals(keycloakId, user.keycloakId)
    }

    @Test
    fun `legacy bot username is upgraded when Telegram username is known`() {
        val telegramId = TelegramId(910484582)
        val legacyUser = createUser(telegramId, Username("telegram_910484582"))
        val username = Username("knalis")
        val fallback = Username("knalis_910484582")

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(legacyUser)
        whenever(usernameResolver.resolve(telegramId, "knalis"))
            .thenReturn(TelegramUsernameResolution(username, fallback))
        whenever(
            keycloakIdentityService.resolveOrCreate(
                telegramId.id,
                username,
                fallback,
            ),
        ).thenReturn(TelegramKeycloakIdentity(legacyUser.keycloakId, username))
        whenever(userRepository.update(legacyUser)).thenReturn(legacyUser)

        val user = service.getOrCreate(telegramId.id, "knalis")

        assertEquals(username, user.username)
        verify(userRepository).update(legacyUser)
    }

    @Test
    fun `existing explicit Mnemos username is not overwritten by bot`() {
        val telegramId = TelegramId(910484582)
        val user = createUser(telegramId, Username("custom_name"))

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)

        val resolved = service.getOrCreate(telegramId.id, "knalis")

        assertEquals(Username("custom_name"), resolved.username)
        verify(usernameResolver, never()).resolve(any(), any())
        verify(keycloakIdentityService, never()).resolveOrCreate(any(), any(), any())
        verify(userRepository, never()).update(any())
    }

    private fun createUser(
        telegramId: TelegramId,
        username: Username,
    ): User =
        User.create(
            telegramId = telegramId,
            username = username,
            keycloakId = KeycloakId(UUID.fromString("632cf054-ac8a-459a-b7af-46f3100cfa67")),
        )
}
