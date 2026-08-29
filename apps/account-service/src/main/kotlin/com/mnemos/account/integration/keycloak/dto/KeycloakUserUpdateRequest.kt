package com.mnemos.account.integration.keycloak.dto

data class KeycloakUserUpdateRequest(
    val username: String,
    val email: String? = null,
    val attributes: Map<String, List<String>>,
)
