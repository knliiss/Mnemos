package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toInternalResponse
import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toQuery
import com.mnemos.account.application.service.account.InternalAccountQueryService
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/accounts")
@PreAuthorize(
    "authentication.name == 'bet-service' or " +
        "authentication.name == 'reward-service' or " +
        "authentication.name == 'wallet-service' or " +
        "authentication.name == 'social-service' or " +
        "authentication.name == 'personalization-service'",
)
class InternalAccountResolveController(
    private val queryService: InternalAccountQueryService,
) {
    @GetMapping("/resolve")
    fun resolve(
        @Valid @ModelAttribute request: ResolveInternalAccountRequest,
    ): ResponseEntity<InternalAccountResponse> {
        val account =
            queryService.resolve(request.toQuery())
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(account.toInternalResponse())
    }
}
