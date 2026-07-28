package com.mnemos.account.integration.keycloak.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserResponse(
    val username: String,
    val enabled: Boolean? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val attributes: Map<String, List<String>>? = null,
)
