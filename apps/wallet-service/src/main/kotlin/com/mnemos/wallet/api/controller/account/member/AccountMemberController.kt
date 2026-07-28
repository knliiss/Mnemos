package com.mnemos.accountservice.api.controller.account.member

import com.mnemos.accountservice.api.dto.account.member.AccountMemberListItemResponse
import com.mnemos.accountservice.api.dto.account.member.toResponse
import com.mnemos.accountservice.application.service.member.AccountMemberQueryService
import com.mnemos.accountservice.application.service.member.KickAccountMemberService
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/account/{accountId}/members")
class AccountMemberController(
    private val memberQueryService: AccountMemberQueryService,
    private val kickAccountMemberService: KickAccountMemberService,
) {
    @GetMapping
    fun getMembers(
        @PathVariable accountId: UUID,
    ): List<AccountMemberListItemResponse> =
        memberQueryService
            .getMembers(AccountId(accountId))
            .map { it.toResponse() }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun kickMember(
        @PathVariable accountId: UUID,
        @PathVariable memberId: UUID,
    ) {
        kickAccountMemberService.kickMember(
            accountId = AccountId(accountId),
            targetMemberId = AccountMemberId(memberId),
        )
    }
}
