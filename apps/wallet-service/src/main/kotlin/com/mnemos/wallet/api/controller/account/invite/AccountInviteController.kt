package com.mnemos.accountservice.api.controller.account.invite

import com.mnemos.accountservice.api.dto.account.invite.AccountInviteListItemResponse
import com.mnemos.accountservice.api.dto.account.invite.CreateAccountInviteRequest
import com.mnemos.accountservice.api.dto.account.invite.CreateAccountInviteResponse
import com.mnemos.accountservice.api.dto.account.invite.toResponse
import com.mnemos.accountservice.application.service.invite.AccountInviteQueryService
import com.mnemos.accountservice.application.service.invite.CreateAccountInviteService
import com.mnemos.accountservice.application.service.invite.RevokeAccountInviteService
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.common.core.model.id.AccountId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/invites")
class AccountInviteController(
    private val createInviteService: CreateAccountInviteService,
    private val inviteQueryService: AccountInviteQueryService,
    private val revokeInviteService: RevokeAccountInviteService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createInvite(
        @PathVariable accountId: UUID,
        @RequestBody @Valid request: CreateAccountInviteRequest,
    ): CreateAccountInviteResponse =
        createInviteService
            .createInvite(
                accountId = AccountId(accountId),
                expiresIn = request.expiresInMinutes?.let(Duration::ofMinutes),
            ).toResponse()

    @GetMapping
    fun getInvites(
        @PathVariable accountId: UUID,
    ): List<AccountInviteListItemResponse> =
        inviteQueryService
            .getInvites(AccountId(accountId))
            .map { it.toResponse() }

    @DeleteMapping("/{inviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeInvite(
        @PathVariable accountId: UUID,
        @PathVariable inviteId: UUID,
    ) {
        revokeInviteService.revokeInvite(
            accountId = AccountId(accountId),
            inviteId = InviteId(inviteId),
        )
    }
}
