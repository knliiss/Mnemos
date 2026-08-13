package com.mnemos.moderation.adapter.out.keycloak

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserResponse(
    val id: String,
    val attributes: Map<String, List<String>> = emptyMap(),
)
