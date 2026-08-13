package com.mnemos.moderation.adapter.out.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.keycloak-admin")
data class KeycloakAdminProperties(
    val url: String,
    val realm: String,
)
