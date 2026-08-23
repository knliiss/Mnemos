package com.mnemos.account.application.service.user

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.account.integration.keycloak.service.KeycloakUserIdentitySynchronizer
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.common.core.model.user.Username
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.security.MnemosPrincipal
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class UserServiceTest {
    @Test
    fun `initializing an existing user does not republish user created event`() {
        val keycloakId = KeycloakId(UUID.fromString("39f20154-d86d-41c5-9659-6986437905bd"))
        val telegramId = TelegramId(931458258)
        val username = Username("knaliz")
        val user =
            User.create(
                telegramId = telegramId,
                username = username,
                keycloakId = keycloakId,
            )
        val principal =
            MnemosPrincipal(
                keycloakId = keycloakId.id,
                username = username.value,
                telegramId = telegramId.id,
                userId = user.id.id,
                roles = setOf(UserRole.USER),
            )
        val userRepository = mock<UserRepository>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val currentMnemosUser = mock<CurrentMnemosUser>()
        val keycloakAdminClient = mock<KeycloakAdminClient>()
        val keycloakAdminProperties = mock<KeycloakAdminProperties>()
        val keycloakTokenProvider = mock<KeycloakTokenProvider>()
        val keycloakUserIdentitySynchronizer = mock<KeycloakUserIdentitySynchronizer>()
        val publisher = mock<IntegrationEventPublisher>()

        whenever(currentMnemosUser.get()).thenReturn(principal)
        whenever(userRepository.findByKeycloakId(keycloakId)).thenReturn(user)
        whenever(keycloakUserIdentitySynchronizer.synchronize(user)).thenReturn(user)

        val service =
            UserService(
                userRepository = userRepository,
                currentUserProvider = currentUserProvider,
                currentMnemosUser = currentMnemosUser,
                keycloakAdminClient = keycloakAdminClient,
                keycloakAdminProperties = keycloakAdminProperties,
                keycloakTokenProvider = keycloakTokenProvider,
                keycloakUserIdentitySynchronizer = keycloakUserIdentitySynchronizer,
                publisher = publisher,
            )

        service.initializeCurrentUser()

        verifyNoInteractions(publisher)
    }
}
