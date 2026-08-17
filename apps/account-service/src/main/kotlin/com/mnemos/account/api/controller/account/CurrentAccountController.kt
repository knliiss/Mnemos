package com.mnemos.account.api.controller.account

import com.mnemos.account.api.dto.account.AccountApiMapper.toResponse
import com.mnemos.account.api.dto.account.AccountDetailsResponse
import com.mnemos.account.api.dto.account.AccountSummaryResponse
import com.mnemos.account.application.service.account.AccountQueryService
import com.mnemos.account.application.service.account.ChangeActiveAccountService
import com.mnemos.account.application.service.account.LeaveActiveAccountService
import com.mnemos.common.core.model.id.AccountId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/me/accounts")
class CurrentAccountController(
    private val accountQueryService: AccountQueryService,
    private val changeActiveAccountService: ChangeActiveAccountService,
    private val leaveActiveAccountService: LeaveActiveAccountService,
) {
    @GetMapping
    fun getAvailableAccounts(): List<AccountSummaryResponse> = accountQueryService.getAvailableAccounts().map { it.toResponse() }

    @GetMapping("/active")
    fun getActiveAccount(): ResponseEntity<AccountDetailsResponse> {
        val account =
            accountQueryService.getCurrentActiveAccount()
                ?: return ResponseEntity.noContent().build()

        return ResponseEntity.ok(account.toResponse())
    }

    @PutMapping("/active/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changeActiveAccount(
        @PathVariable accountId: UUID,
    ) {
        changeActiveAccountService.changeActiveAccount(AccountId(accountId))
    }

    @PostMapping("/active/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun leaveActiveAccount() {
        leaveActiveAccountService.leaveActiveAccount()
    }
}
