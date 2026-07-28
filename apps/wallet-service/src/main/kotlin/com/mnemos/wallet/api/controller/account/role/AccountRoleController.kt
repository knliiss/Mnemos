package com.mnemos.accountservice.api.controller.account.role

import com.mnemos.accountservice.api.dto.account.role.CreateRoleRequest
import com.mnemos.accountservice.api.dto.account.role.RoleResponse
import com.mnemos.accountservice.api.dto.account.role.UpdateRoleDetailsRequest
import com.mnemos.accountservice.api.dto.account.role.UpdateRoleHierarchyRequest
import com.mnemos.accountservice.api.dto.account.role.UpdateRolePermissionsRequest
import com.mnemos.accountservice.api.dto.account.role.toResponse
import com.mnemos.accountservice.application.service.role.CreateRoleService
import com.mnemos.accountservice.application.service.role.DeleteAccountRoleService
import com.mnemos.accountservice.application.service.role.RoleQueryService
import com.mnemos.accountservice.application.service.role.UpdateRoleHierarchyService
import com.mnemos.accountservice.application.service.role.UpdateRoleService
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
@RequestMapping("/api/v1/accounts/{accountId}/roles")
class AccountRoleController(
    private val roleQueryService: RoleQueryService,
    private val createRoleService: CreateRoleService,
    private val updateRoleService: UpdateRoleService,
    private val deleteRoleService: DeleteAccountRoleService,
    private val updateRoleHierarchyService: UpdateRoleHierarchyService,
) {
    @GetMapping
    fun getRoles(
        @PathVariable accountId: UUID,
    ): List<RoleResponse> =
        roleQueryService
            .getRoles(AccountId(accountId))
            .map { it.toResponse() }

    @GetMapping("/{roleId}")
    fun getRole(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
    ): RoleResponse =
        roleQueryService
            .getRole(
                accountId = AccountId(accountId),
                roleId = RoleId(roleId),
            ).toResponse()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRole(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: CreateRoleRequest,
    ): RoleResponse =
        createRoleService
            .createRole(
                accountId = AccountId(accountId),
                name = request.name,
                color = request.color,
                permissions = request.permissions,
            ).toResponse()

    @PatchMapping("/{roleId}")
    fun updateRoleDetails(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
        @Valid @RequestBody request: UpdateRoleDetailsRequest,
    ): RoleResponse =
        updateRoleService
            .updateDetails(
                accountId = AccountId(accountId),
                roleId = RoleId(roleId),
                name = request.name,
                color = request.color,
            ).toResponse()

    @PatchMapping("/{roleId}/permissions")
    fun updateRolePermissions(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
        @Valid @RequestBody request: UpdateRolePermissionsRequest,
    ): RoleResponse =
        updateRoleService
            .updatePermissions(
                accountId = AccountId(accountId),
                roleId = RoleId(roleId),
                permissions = request.permissions,
            ).toResponse()

    @PutMapping("/hierarchy")
    fun updateHierarchy(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: UpdateRoleHierarchyRequest,
    ): List<RoleResponse> =
        updateRoleHierarchyService
            .updateHierarchy(
                accountId = AccountId(accountId),
                orderedRoleIds = request.orderedRoleIds.map(::RoleId),
            ).map { it.toResponse() }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRole(
        @PathVariable accountId: UUID,
        @PathVariable roleId: UUID,
    ) {
        deleteRoleService.deleteRole(
            accountId = AccountId(accountId),
            roleId = RoleId(roleId),
        )
    }
}
