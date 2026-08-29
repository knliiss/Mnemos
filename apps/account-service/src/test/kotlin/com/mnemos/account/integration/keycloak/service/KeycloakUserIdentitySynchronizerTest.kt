package com.mnemos.account.integration.keycloak.service

import com.mnemos.account.domain.model.user.User
import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.account.integration.keycloak.dto.KeycloakUserResponse
import com.mnemos.account.integration.keycloak.dto.KeycloakUserUpdateRequest
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class KeycloakUserIdentitySynchronizerTest {
    @Test
    fun `synchronization updates only username and custom attributes`() {
        val keycloakId = KeycloakId(UUID.fromString("6896dda5-e7e4-46dc-bc5d-83b042c80367"))
        val user =
            User.create(
                telegramId = TelegramId(6591739840),
                username = Username("lunkal0ve"),
                keycloakId = keycloakId,
            )
        val keycloakAdminClient = mock<KeycloakAdminClient>()
        val keycloakTokenProvider = mock<KeycloakTokenProvider>()
        val keycloakAdminProperties =
            KeycloakAdminProperties(
                url = "http://keycloak:8080",
                realm = "mnemos",
            )
        val telegramDisplayName = "🐾 luster | ᑫᵘⁱⁿᵗᵉᵗ ˡⁱᵛᵉ 🐾"
        val existingAttributes =
            mapOf(
                "telegram_user_id" to listOf("6591739840"),
                "telegram_first_name" to listOf(telegramDisplayName),
            )
        val keycloakUser =
            KeycloakUserResponse(
                username = "lunkal0ve",
                attributes = existingAttributes,
            )

        whenever(keycloakTokenProvider.token()).thenReturn("service-token")
        whenever(
            keycloakAdminClient.getUser(
                authorization = "Bearer service-token",
                realm = "mnemos",
                userId = keycloakId.id,
            ),
        ).thenReturn(keycloakUser)

        val synchronizer =
            KeycloakUserIdentitySynchronizer(
                keycloakAdminClient = keycloakAdminClient,
                keycloakAdminProperties = keycloakAdminProperties,
                keycloakTokenProvider = keycloakTokenProvider,
            )

        synchronizer.synchronize(user)

        val request = argumentCaptor<KeycloakUserUpdateRequest>()

        verify(keycloakAdminClient).getUser(
            authorization = "Bearer service-token",
            realm = "mnemos",
            userId = keycloakId.id,
        )
        verify(keycloakAdminClient).updateUser(
            authorization = "Bearer service-token",
            realm = "mnemos",
            userId = keycloakId.id,
            request = request.capture(),
        )
        verifyNoMoreInteractions(keycloakAdminClient)

        assertEquals("lunkal0ve", request.firstValue.username)
        assertEquals("6591739840", request.firstValue.attributes["telegram_user_id"]?.single())
        assertEquals(telegramDisplayName, request.firstValue.attributes["telegram_first_name"]?.single())
        assertEquals(user.id.id.toString(), request.firstValue.attributes["mnemos_user_id"]?.single())
    }
}
