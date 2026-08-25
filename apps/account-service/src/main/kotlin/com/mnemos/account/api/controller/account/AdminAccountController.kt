package com.mnemos.account.api.controller.account

import com.mnemos.account.application.service.account.DeactivateAccountService
import com.mnemos.account.application.service.account.ReactivateAccountService
import com.mnemos.common.core.model.id.AccountId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts")
class AdminAccountController(
    private val reactivateAccountService: ReactivateAccountService,
    private val deactivateAccountService: DeactivateAccountService,
) {
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
}
