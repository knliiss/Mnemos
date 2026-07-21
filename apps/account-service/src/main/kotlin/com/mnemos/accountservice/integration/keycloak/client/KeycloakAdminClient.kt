package com.mnemos.accountservice.integration.keycloak.client

import com.mnemos.accountservice.integration.keycloak.dto.KeycloakUserResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@FeignClient(
    name = "keycloakAdminClient",
    url = "\${mnemos.keycloak-admin.url}"
)
interface KeycloakAdminClient {

    @GetMapping("/admin/realms/{realm}/users/{userId}")
    fun getUser(
        @PathVariable realm: String,
        @PathVariable userId: UUID
    ): KeycloakUserResponse

    @PutMapping("/admin/realms/{realm}/users/{userId}")
    fun updateUser(
        @PathVariable realm: String,
        @PathVariable userId: UUID,
        @RequestBody request: KeycloakUserResponse
    )
}