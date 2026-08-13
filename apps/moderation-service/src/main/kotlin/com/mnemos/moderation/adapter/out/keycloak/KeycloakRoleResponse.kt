package com.mnemos.moderation.adapter.out.keycloak

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakRoleResponse(
    val name: String,
)
