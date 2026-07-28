package com.mnemos.account.api.controller.account

import com.mnemos.account.api.dto.account.CreateAccountRequest
import com.mnemos.account.api.dto.account.CreateAccountResponse
import com.mnemos.account.api.dto.account.UpdateAccountBaseRoleRequest
import com.mnemos.account.api.dto.account.toResponse
import com.mnemos.account.application.service.account.CreateAccountService
import com.mnemos.account.application.service.account.DeleteAccountService
import com.mnemos.account.application.service.account.TransferAccountOwnerShipService
import com.mnemos.account.application.service.account.UpdateAccountBaseRoleService
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
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
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val createAccountService: CreateAccountService,
    private val deleteAccountService: DeleteAccountService,
    private val transferAccountOwnershipService: TransferAccountOwnerShipService,
    private val updateAccountBaseRoleService: UpdateAccountBaseRoleService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(
        @RequestBody @Valid request: CreateAccountRequest,
    ): CreateAccountResponse = createAccountService.createAccount(request).toResponse()

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAccount(
        @PathVariable accountId: UUID,
    ) {
        deleteAccountService.deleteAccount(AccountId(accountId))
    }

    @PutMapping("/{accountId}/owner/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun transferOwnership(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
    ) {
        transferAccountOwnershipService.transfer(
            accountId = AccountId(accountId),
            newOwnerMemberId = AccountMemberId(memberId),
        )
    }

    @PatchMapping("/{accountId}/base-role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateBaseRole(
        @PathVariable accountId: UUID,
        @RequestBody request: UpdateAccountBaseRoleRequest,
    ) {
        updateAccountBaseRoleService.updateBaseRole(
            accountId = AccountId(accountId),
            roleId = request.roleId?.let(::RoleId),
        )
    }
}
