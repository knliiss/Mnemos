package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toInternalResponse
import com.mnemos.account.application.service.account.InternalAccountQueryService
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/accounts")
class InternalAccountResolveController(
    private val queryService: InternalAccountQueryService,
) {
    @GetMapping("/resolve")
    fun resolve(
        @RequestParam(required = false) accountId: UUID?,
        @RequestParam(required = false) userId: UUID?,
        @RequestParam(required = false) cristalixNickname: String?,
    ): ResponseEntity<InternalAccountResponse> {
        val providedLookupKeys = listOf(accountId, userId, cristalixNickname).count { it != null }

        require(providedLookupKeys == 1) {
            "Exactly one account lookup key must be provided"
        }

        val account =
            when {
                accountId != null -> queryService.findActiveByAccountId(accountId)
                userId != null -> queryService.findActiveByUserId(userId)
                else -> queryService.findActiveByCristalixNickname(requireNotNull(cristalixNickname))
            } ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(account.toInternalResponse())
    }
}
