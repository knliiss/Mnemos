package com.mnemos.accountservice.api.controller.account.role

import com.mnemos.accountservice.api.dto.account.role.RoleResponse
import com.mnemos.accountservice.api.dto.account.role.toResponse
import com.mnemos.accountservice.application.service.role.AssignRoleService
import com.mnemos.accountservice.application.service.role.RoleQueryService
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/members/{memberId}/roles")
class AccountMemberRoleController(
    private val roleQueryService: RoleQueryService,
    private val assignRoleService: AssignRoleService,
) {
    @GetMapping
    fun getMemberRoles(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
    ): List<RoleResponse> =
        roleQueryService
            .getMemberRoles(
                accountId = AccountId(accountId),
                memberId = AccountMemberId(memberId),
            ).map { it.toResponse() }

    @PutMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun assignRole(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ) {
        assignRoleService.assignRole(
            accountId = AccountId(accountId),
            memberId = AccountMemberId(memberId),
            roleId = RoleId(roleId),
        )
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeRole(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
        @PathVariable roleId: UUID,
    ) {
        assignRoleService.revokeRole(
            accountId = AccountId(accountId),
            memberId = AccountMemberId(memberId),
            roleId = RoleId(roleId),
        )
    }
}
