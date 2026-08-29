package com.mnemos.account.integration.keycloak.dto

data class KeycloakUserUpdateRequest(
    val username: String,
    val attributes: Map<String, List<String>>,
)
