package com.mnemos.account.integration.keycloak.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserResponse(
    val username: String,
    val attributes: Map<String, List<String>>? = null,
)
