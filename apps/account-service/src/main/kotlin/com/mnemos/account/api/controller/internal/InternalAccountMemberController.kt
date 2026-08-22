package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.service.account.InternalAccountMemberQueryService
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.internal.v1.InternalAccountMemberUserIdsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/accounts")
class InternalAccountMemberController(
    private val queryService: InternalAccountMemberQueryService,
) {
    @GetMapping("/{accountId}/members/user-ids")
    fun findMemberUserIds(
        @PathVariable accountId: UUID,
    ): ResponseEntity<InternalAccountMemberUserIdsResponse> {
        val userIds =
            queryService.findUserIds(AccountId(accountId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalAccountMemberUserIdsResponse(
                userIds = userIds.map { it.id },
            ),
        )
    }
}
