package com.mnemos.account.application.service

import com.mnemos.account.domain.exception.UserIdentityConflictException
import com.mnemos.account.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.account.integration.keycloak.config.KeycloakAdminProperties
import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import com.mnemos.account.integration.keycloak.dto.KeycloakUserResponse
import com.mnemos.account.support.InMemoryUserRepository
import com.mnemos.account.support.SecurityContextTest
import com.mnemos.account.support.authenticate
import com.mnemos.account.support.currentUserProvider
import com.mnemos.account.support.user
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.security.MnemosSecurityProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.Instant

class UserServiceTest : SecurityContextTest() {
    @Test
    fun `initialize returns existing user without creating duplicate`() {
        val existing = user()
        val repository = InMemoryUserRepository(listOf(existing))
        authenticate(existing)
        val service = service(repository)

        val result = service.initializeCurrentUser()

        assertEquals(existing.id, result.id)
        assertEquals(0, repository.createCount)
    }

    @Test
    fun `initialize creates local user from JWT identity`() {
        val principal = user(username = Username("new_user"))
        val repository = InMemoryUserRepository()
        authenticate(principal)
        val service = service(repository)

        val result = service.initializeCurrentUser()

        assertEquals(principal.keycloakId, repository.users.getValue(result.id).keycloakId)
        assertEquals(principal.telegramId, repository.users.getValue(result.id).telegramId)
        assertEquals(principal.username, result.username)
        assertEquals(1, repository.createCount)
    }

    @Test
    fun `initialize rejects telegram identity already linked to another keycloak user`() {
        val existing = user()
        val principal = user(telegramId = existing.telegramId)
        val repository = InMemoryUserRepository(listOf(existing))
        authenticate(principal)

        val exception =
            assertThrows<UserIdentityConflictException> {
                service(repository).initializeCurrentUser()
            }

        assertEquals(existing.telegramId.value, exception.details["telegramId"])
        assertEquals(0, repository.createCount)
    }

    @Test
    fun `initialize rejects username case-insensitive conflict`() {
        val existing = user(username = Username("Existing"))
        val principal = user(username = Username("existing"))
        val repository = InMemoryUserRepository(listOf(existing))
        authenticate(principal)

        assertThrows<UserIdentityConflictException> { service(repository).initializeCurrentUser() }
        assertEquals(0, repository.createCount)
    }

    @Test
    fun `get current user delegates to provider`() {
        val current = user()
        val repository = InMemoryUserRepository(listOf(current))
        authenticate(current)

        assertEquals(current.id, service(repository).getCurrentUser().id)
    }

    @Test
    fun `update username returns unchanged user without persistence or keycloak calls`() {
        val current = user(username = Username("same_name"))
        val repository = InMemoryUserRepository(listOf(current))
        val keycloak = RecordingKeycloakClient()
        authenticate(current)
        val service = service(repository, keycloak)

        val result = service.updateUsername(Username("same_name"))

        assertEquals(current.username, result.username)
        assertEquals(0, repository.updateCount)
        assertEquals(0, keycloak.getCalls)
        assertEquals(0, keycloak.updateCalls)
    }

    @Test
    fun `update username rejects case-insensitive conflict`() {
        val current = user(username = Username("current"))
        val other = user(username = Username("TAKEN"))
        val repository = InMemoryUserRepository(listOf(current, other))
        authenticate(current)

        assertThrows<UserIdentityConflictException> {
            service(repository).updateUsername(Username("taken"))
        }
        assertEquals(0, repository.updateCount)
    }

    @Test
    fun `update username persists and synchronizes complete keycloak representation`() {
        val current = user(username = Username("old_name"))
        val repository = InMemoryUserRepository(listOf(current))
        val originalRepresentation =
            KeycloakUserResponse(
                username = "old_name",
                enabled = true,
                firstName = "First",
                lastName = "Last",
                email = "user@example.com",
                attributes = mapOf("telegram_id" to listOf(current.telegramId.value.toString())),
            )
        val keycloak = RecordingKeycloakClient(originalRepresentation)
        authenticate(current)

        val result = service(repository, keycloak).updateUsername(Username("new_name"))

        assertEquals(Username("new_name"), result.username)
        assertEquals(1, repository.updateCount)
        assertEquals(1, keycloak.getCalls)
        assertEquals(1, keycloak.updateCalls)
        assertEquals(originalRepresentation.copy(username = "new_name"), keycloak.lastUpdate)
        assertEquals("Bearer test-access-token", keycloak.lastAuthorization)
    }

    @Test
    fun `update localization is idempotent`() {
        val current = user(localizationLanguage = LocalizationLanguage.EN)
        val repository = InMemoryUserRepository(listOf(current))
        authenticate(current)

        val result = service(repository).updateLocalization(LocalizationLanguage.EN)

        assertEquals(LocalizationLanguage.EN, result.localizationLanguage)
        assertEquals(0, repository.updateCount)
    }

    @Test
    fun `update localization persists changed language`() {
        val current = user(localizationLanguage = LocalizationLanguage.EN)
        val repository = InMemoryUserRepository(listOf(current))
        authenticate(current)

        val result = service(repository).updateLocalization(LocalizationLanguage.UK)

        assertEquals(LocalizationLanguage.UK, result.localizationLanguage)
        assertEquals(1, repository.updateCount)
        assertSame(current, repository.users[current.id])
    }

    private fun service(
        repository: InMemoryUserRepository,
        keycloakClient: RecordingKeycloakClient = RecordingKeycloakClient(),
    ): UserService {
        val currentMnemosUser = CurrentMnemosUser(MnemosSecurityProperties())
        return UserService(
            repository,
            currentUserProvider(repository),
            currentMnemosUser,
            keycloakClient,
            KeycloakAdminProperties("http://keycloak", "mnemos"),
            tokenProvider(),
        )
    }

    private fun tokenProvider(): KeycloakTokenProvider {
        val registration =
            ClientRegistration
                .withRegistrationId("keycloak-admin")
                .tokenUri("http://keycloak/token")
                .clientId("client")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build()
        val token =
            OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
            )
        val manager =
            OAuth2AuthorizedClientManager {
                OAuth2AuthorizedClient(registration, "keycloak-admin", token)
            }
        return KeycloakTokenProvider(manager)
    }

    private class RecordingKeycloakClient(
        private val response: KeycloakUserResponse = KeycloakUserResponse("existing"),
    ) : KeycloakAdminClient {
        var getCalls = 0
        var updateCalls = 0
        var lastUpdate: KeycloakUserResponse? = null
        var lastAuthorization: String? = null

        override fun getUser(
            authorization: String,
            realm: String,
            userId: java.util.UUID,
        ): KeycloakUserResponse {
            getCalls++
            lastAuthorization = authorization
            return response
        }

        override fun updateUser(
            authorization: String,
            realm: String,
            userId: java.util.UUID,
            request: KeycloakUserResponse,
        ) {
            updateCalls++
            lastAuthorization = authorization
            lastUpdate = request
        }
    }
}
