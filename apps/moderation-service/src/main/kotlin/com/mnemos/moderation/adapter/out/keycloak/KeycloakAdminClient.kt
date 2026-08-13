package com.mnemos.moderation.adapter.out.keycloak

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "moderationKeycloakAdminClient",
    url = "\${mnemos.keycloak-admin.url}",
)
interface KeycloakAdminClient {
    @GetMapping("/admin/realms/{realm}/users")
    fun findUsers(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        authorization: String,
        @PathVariable
        realm: String,
        @RequestParam("q")
        query: String,
    ): List<KeycloakUserResponse>

    @GetMapping("/admin/realms/{realm}/users/{keycloakUserId}/role-mappings/realm/composite")
    fun getEffectiveRealmRoles(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        authorization: String,
        @PathVariable
        realm: String,
        @PathVariable
        keycloakUserId: String,
    ): List<KeycloakRoleResponse>
}
