package com.mnemos.account.integration.keycloak.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserResponse(
    val id: UUID? = null,
    val username: String,
    val email: String? = null,
    val attributes: Map<String, List<String>>? = null,
)
