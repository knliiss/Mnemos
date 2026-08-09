package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.dto.internal.permission.CheckAccountPermissionByKeycloakIdRequest
import com.mnemos.account.api.dto.internal.permission.CheckAccountPermissionRequest
import com.mnemos.account.api.dto.internal.permission.CheckAccountPermissionResponse
import com.mnemos.account.api.dto.internal.permission.EffectivePermissionsResponse
import com.mnemos.account.application.service.permission.AccountPermissionQueryService
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.UserId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/accounts/{accountId}/permissions")
class InternalAccountPermissionController(
    private val permissionQueryService: AccountPermissionQueryService,
) {
    @PostMapping("/check")
    fun checkPermission(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionRequest,
    ): CheckAccountPermissionResponse =
        CheckAccountPermissionResponse(
            granted =
                permissionQueryService.hasPermission(
                    accountId = AccountId(accountId),
                    userId = UserId(request.userId),
                    permission = request.permission,
                ),
        )

    @GetMapping("/users/{userId}")
    fun getEffectivePermissions(
        @PathVariable accountId: UUID,
        @PathVariable userId: UUID,
    ): EffectivePermissionsResponse =
        EffectivePermissionsResponse(
            permissions =
                permissionQueryService.getEffectivePermissions(
                    accountId = AccountId(accountId),
                    userId = UserId(userId),
                ),
        )

    @PostMapping("/check-by-keycloak-id")
    fun checkPermissionByKeycloakId(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionByKeycloakIdRequest,
    ): CheckAccountPermissionResponse =
        CheckAccountPermissionResponse(
            granted =
                permissionQueryService.hasPermissionByKeycloakId(
                    accountId = AccountId(accountId),
                    keycloakId = KeycloakId(request.keycloakId),
                    permission = request.permission,
                ),
        )
}
