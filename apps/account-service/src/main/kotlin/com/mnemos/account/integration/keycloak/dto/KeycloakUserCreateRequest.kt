package com.mnemos.account.integration.keycloak.dto

data class KeycloakUserCreateRequest(
    val username: String,
    val enabled: Boolean = true,
    val attributes: Map<String, List<String>>,
)
