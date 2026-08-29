package com.mnemos.account.integration.keycloak.client

import com.mnemos.account.integration.keycloak.dto.KeycloakUserResponse
import com.mnemos.account.integration.keycloak.dto.KeycloakUserUpdateRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.util.UUID

@FeignClient(
    name = "keycloakAdminClient",
    url = "\${mnemos.keycloak-admin.url}",
)
interface KeycloakAdminClient {
    @GetMapping("/admin/realms/{realm}/users/{userId}")
    fun getUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @PathVariable realm: String,
        @PathVariable userId: UUID,
    ): KeycloakUserResponse

    @PutMapping("/admin/realms/{realm}/users/{userId}")
    fun updateUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @PathVariable realm: String,
        @PathVariable userId: UUID,
        @RequestBody request: KeycloakUserUpdateRequest,
    )
}
