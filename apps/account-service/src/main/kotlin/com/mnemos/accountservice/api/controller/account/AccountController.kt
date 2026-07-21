package com.mnemos.accountservice.api.controller.account

import com.mnemos.accountservice.api.dto.account.AccountResponse
import com.mnemos.accountservice.api.dto.account.CreateAccountRequest
import com.mnemos.accountservice.application.service.account.CreateAccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val createAccountService: CreateAccountService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(
        @RequestBody @Valid request: CreateAccountRequest
    ): AccountResponse {
       return createAccountService.createAccount(
            request
        )
    }

//    @PostMapping

}