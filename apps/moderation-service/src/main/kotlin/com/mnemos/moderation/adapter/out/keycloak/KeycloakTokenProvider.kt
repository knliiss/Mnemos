package com.mnemos.moderation.adapter.out.keycloak

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component

@Component
class KeycloakTokenProvider(
    private val manager: OAuth2AuthorizedClientManager,
) {
    fun token(): String =
        requireNotNull(
            manager.authorize(
                OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak-moderation")
                    .principal("keycloak-moderation")
                    .build(),
            ),
        ).accessToken
            .tokenValue
}
