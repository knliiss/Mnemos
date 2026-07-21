package com.mnemos.accountservice.integration.keycloak.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties::class)
class KeycloakFeignConfig {
}