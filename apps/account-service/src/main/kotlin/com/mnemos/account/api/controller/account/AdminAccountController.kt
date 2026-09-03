package com.mnemos.account.api.controller.account

import com.mnemos.account.api.dto.account.AdminAccountDetailsResponse
import com.mnemos.account.application.service.account.AdminAccountManagementService
import com.mnemos.account.application.service.account.DeactivateAccountService
import com.mnemos.account.application.service.account.ReactivateAccountService
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts")
class AdminAccountController(
    private val reactivateAccountService: ReactivateAccountService,
    private val deactivateAccountService: DeactivateAccountService,
    private val adminAccountManagementService: AdminAccountManagementService,
) {
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun getDetails(
        @PathVariable accountId: UUID,
    ): AdminAccountDetailsResponse = adminAccountManagementService.getDetails(AccountId(accountId))

    @PostMapping("/{accountId}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun reactivate(
        @PathVariable accountId: UUID,
    ) {
        reactivateAccountService.reactivate(AccountId(accountId))
    }

    @PostMapping("/{accountId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun deactivate(
        @PathVariable accountId: UUID,
    ) {
        deactivateAccountService.deactivate(AccountId(accountId))
    }

    @PostMapping("/{accountId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun addMember(
        @PathVariable accountId: UUID,
        @PathVariable userId: UUID,
    ) {
        adminAccountManagementService.addMember(
            accountId = AccountId(accountId),
            userId = UserId(userId),
        )
    }

    @DeleteMapping("/{accountId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun removeMember(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
    ) {
        adminAccountManagementService.removeMember(
            accountId = AccountId(accountId),
            memberId = AccountMemberId(memberId),
        )
    }

    @PutMapping("/{accountId}/owner/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun transferOwnership(
        @PathVariable accountId: UUID,
        @PathVariable userId: UUID,
    ) {
        adminAccountManagementService.transferOwnership(
            accountId = AccountId(accountId),
            newOwnerUserId = UserId(userId),
        )
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun deleteAccount(
        @PathVariable accountId: UUID,
    ) {
        adminAccountManagementService.deleteAccount(AccountId(accountId))
    }
}
