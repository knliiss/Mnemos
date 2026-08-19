package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toInternalResponse
import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toQuery
import com.mnemos.account.application.service.account.InternalAccountQueryService
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/accounts")
class InternalAccountResolveController(
    private val queryService: InternalAccountQueryService,
) {
    @GetMapping("/resolve")
    fun resolve(
        @Valid @ModelAttribute request: ResolveInternalAccountRequest,
    ): ResponseEntity<InternalAccountResponse> {
        val account = queryService.resolve(request.toQuery())
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(account.toInternalResponse())
    }
}
