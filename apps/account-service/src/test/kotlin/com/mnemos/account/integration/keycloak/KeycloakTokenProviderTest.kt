package com.mnemos.account.integration.keycloak

import com.mnemos.account.integration.keycloak.config.KeycloakTokenProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.Instant

class KeycloakTokenProviderTest {
    @Test
    fun `returns access token from client credentials authorization`() {
        var registrationId: String? = null
        var principalName: String? = null
        val manager =
            OAuth2AuthorizedClientManager { request ->
                registrationId = request.clientRegistrationId
                principalName = request.principal.name
                authorizedClient("admin-token")
            }

        val token = KeycloakTokenProvider(manager).token()

        assertEquals("admin-token", token)
        assertEquals("keycloak-admin", registrationId)
        assertEquals("keycloak-admin", principalName)
    }

    @Test
    fun `fails explicitly when client cannot be authorized`() {
        val manager = OAuth2AuthorizedClientManager { null }

        val error =
            assertThrows<IllegalStateException> {
                KeycloakTokenProvider(manager).token()
            }

        assertEquals("Cannot authorize keycloak-admin", error.message)
    }

    private fun authorizedClient(token: String): OAuth2AuthorizedClient {
        val registration =
            ClientRegistration
                .withRegistrationId("keycloak-admin")
                .tokenUri("https://keycloak/token")
                .clientId("account-service")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build()
        val accessToken =
            OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                token,
                Instant.now(),
                Instant.now().plusSeconds(60),
            )
        return OAuth2AuthorizedClient(registration, "keycloak-admin", accessToken)
    }
}
