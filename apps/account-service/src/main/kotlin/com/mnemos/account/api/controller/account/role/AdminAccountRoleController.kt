package com.mnemos.account.api.controller.account.role

import com.mnemos.account.api.dto.account.role.CreateRoleRequest
import com.mnemos.account.api.dto.account.role.RoleApiMapper.toResponse
import com.mnemos.account.api.dto.account.role.RoleResponse
import com.mnemos.account.api.dto.account.role.UpdateRoleDetailsRequest
import com.mnemos.account.api.dto.account.role.UpdateRoleHierarchyRequest
import com.mnemos.account.api.dto.account.role.UpdateRolePermissionsRequest
import com.mnemos.account.application.service.role.AdminRoleManagementService
import com.mnemos.account.application.service.role.AdminRoleQueryService
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts/{accountId}")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminAccountRoleController(
    private val adminRoleQueryService: AdminRoleQueryService,
    private val adminRoleManagementService: AdminRoleManagementService,
) {
    @GetMapping("/roles")
    fun getRoles(
        @PathVariable accountId: UUID,
    ): List<RoleResponse> =
        adminRoleQueryService
            .getRoles(AccountId(accountId))
            .map { it.toResponse() }

    @GetMapping("/roles/{roleId}")
    fun getRole(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
    ): RoleResponse =
        adminRoleQueryService
            .getRole(
                accountId = AccountId(accountId),
                roleId = RoleId(roleId),
            ).toResponse()

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRole(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: CreateRoleRequest,
    ): RoleResponse =
        adminRoleManagementService
            .createRole(
                accountId = AccountId(accountId),
                name = request.name,
                color = request.color,
                permissions = request.permissions,
            ).toResponse()

    @PatchMapping("/roles/{roleId}")
    fun updateRoleDetails(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
        @Valid @RequestBody request: UpdateRoleDetailsRequest,
    ): RoleResponse =
        adminRoleManagementService
            .updateDetails(
                accountId = AccountId(accountId),
                roleId = RoleId(roleId),
                name = request.name,
                color = request.color,
            ).toResponse()

    @PatchMapping("/roles/{roleId}/permissions")
    fun updateRolePermissions(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
        @Valid @RequestBody request: UpdateRolePermissionsRequest,
    ): RoleResponse =
        adminRoleManagementService
            .updatePermissions(
                accountId = AccountId(accountId),
                roleId = RoleId(roleId),
                permissions = request.permissions,
            ).toResponse()

    @PutMapping("/roles/hierarchy")
    fun updateRoleHierarchy(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: UpdateRoleHierarchyRequest,
    ): List<RoleResponse> =
        adminRoleManagementService
            .updateHierarchy(
                accountId = AccountId(accountId),
                orderedRoleIds = request.orderedRoleIds.map(::RoleId),
            ).map { it.toResponse() }

    @PutMapping("/roles/base/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateBaseRole(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
    ) {
        adminRoleManagementService.updateBaseRole(
            accountId = AccountId(accountId),
            roleId = RoleId(roleId),
        )
    }

    @DeleteMapping("/roles/base")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clearBaseRole(
        @PathVariable accountId: UUID,
    ) {
        adminRoleManagementService.updateBaseRole(
            accountId = AccountId(accountId),
            roleId = null,
        )
    }

    @DeleteMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRole(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
    ) {
        adminRoleManagementService.deleteRole(
            accountId = AccountId(accountId),
            roleId = RoleId(roleId),
        )
    }

    @GetMapping("/members/{memberId}/roles")
    fun getMemberRoles(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
    ): List<RoleResponse> =
        adminRoleQueryService
            .getMemberRoles(
                accountId = AccountId(accountId),
                memberId = AccountMemberId(memberId),
            ).map { it.toResponse() }

    @PutMapping("/members/{memberId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun assignMemberRole(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ) {
        adminRoleManagementService.assignRole(
            accountId = AccountId(accountId),
            memberId = AccountMemberId(memberId),
            roleId = RoleId(roleId),
        )
    }

    @DeleteMapping("/members/{memberId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeMemberRole(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ) {
        adminRoleManagementService.revokeRole(
            accountId = AccountId(accountId),
            memberId = AccountMemberId(memberId),
            roleId = RoleId(roleId),
        )
    }
}
