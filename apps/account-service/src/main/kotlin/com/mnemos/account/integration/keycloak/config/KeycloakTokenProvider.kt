package com.mnemos.account.integration.keycloak.config

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component

@Component
class KeycloakTokenProvider(
    private val manager: OAuth2AuthorizedClientManager,
) {
    fun token(): String {
        val client =
            manager.authorize(
                OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak-admin")
                    .principal("keycloak-admin")
                    .build(),
            ) ?: error("Cannot authorize keycloak-admin")

        return client.accessToken.tokenValue
    }
}
