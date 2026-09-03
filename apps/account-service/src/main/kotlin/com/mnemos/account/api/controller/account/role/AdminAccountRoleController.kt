package com.mnemos.account.api.controller.account.role

import com.mnemos.account.api.dto.account.role.RoleApiMapper.toResponse
import com.mnemos.account.api.dto.account.role.RoleResponse
import com.mnemos.account.application.service.role.AdminRoleQueryService
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts/{accountId}")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminAccountRoleController(
    private val adminRoleQueryService: AdminRoleQueryService,
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
}
