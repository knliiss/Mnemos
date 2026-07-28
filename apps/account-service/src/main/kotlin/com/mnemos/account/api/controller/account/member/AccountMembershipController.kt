package com.mnemos.account.api.controller.account.member

import com.mnemos.account.api.dto.account.member.JoinAccountRequest
import com.mnemos.account.api.dto.account.member.JoinAccountResponse
import com.mnemos.account.api.dto.account.member.toResponse
import com.mnemos.account.application.service.member.JoinAccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/account-memberships")
class AccountMembershipController(
    private val joinAccountService: JoinAccountService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun joinAccount(
        @Valid @RequestBody request: JoinAccountRequest,
    ): JoinAccountResponse = joinAccountService.joinAccount(request.inviteCode).toResponse()
}
