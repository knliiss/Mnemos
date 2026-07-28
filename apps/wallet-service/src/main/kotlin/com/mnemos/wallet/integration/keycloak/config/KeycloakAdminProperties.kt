package com.mnemos.accountservice.integration.keycloak.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mnemos.keycloak-admin")
data class KeycloakAdminProperties(
    val url: String,
    val realm: String,
)
