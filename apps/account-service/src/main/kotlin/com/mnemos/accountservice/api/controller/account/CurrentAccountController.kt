package com.mnemos.accountservice.api.controller.account

import com.mnemos.accountservice.api.dto.account.AccountResponse
import com.mnemos.accountservice.api.dto.account.AvailableAccountResponse
import com.mnemos.accountservice.application.service.account.AccountQueryService
import com.mnemos.accountservice.application.service.account.ChangeActiveAccountService
import com.mnemos.accountservice.application.service.account.LeaveActiveAccountService
import com.mnemos.common.core.model.id.AccountId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/me/accounts")
class CurrentAccountController(
    val accountQueryService: AccountQueryService,
    val changeActiveAccountService: ChangeActiveAccountService,
    val leaveActiveAccountService: LeaveActiveAccountService
) {

    @GetMapping
    fun getAvailableAccounts()
    : List<AvailableAccountResponse> = accountQueryService.getAvailableAccounts()

    @GetMapping("/active")
    fun getActiveAccount(): ResponseEntity<AccountResponse> {
        val account = accountQueryService.getCurrentActiveAccount()
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(account)
    }

    @PutMapping("/active/{accountId}")
    fun changeActiveAccount(
        @PathVariable accountId: UUID
    ): AccountResponse {
        return changeActiveAccountService.changeActiveAccount(AccountId(accountId))
    }

    @PostMapping("/active/leave")
    fun leaveActiveAccount(): List<AvailableAccountResponse> {
        return leaveActiveAccountService.leaveActiveAccount()
    }

}