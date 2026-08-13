package com.mnemos.moderation.adapter.out.keycloak

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeycloakAdminProperties::class)
class KeycloakAdminConfiguration {
    @Bean
    fun authorizedClientManager(
        registrations: ClientRegistrationRepository,
        clients: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val manager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                registrations,
                clients,
            )

        manager.setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build(),
        )

        return manager
    }
}
