package com.mnemos.account.integration.keycloak.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mnemos.keycloak-admin")
data class KeycloakAdminProperties(
    val url: String,
    val realm: String,
)
