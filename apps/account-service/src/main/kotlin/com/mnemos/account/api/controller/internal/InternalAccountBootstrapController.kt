package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toBootstrapResponse
import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toQuery
import com.mnemos.account.application.service.account.InternalAccountQueryService
import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/accounts/bootstrap")
@PreAuthorize("authentication.name == 'profile-service'")
class InternalAccountBootstrapController(
    private val queryService: InternalAccountQueryService,
) {
    @GetMapping
    fun bootstrap(
        @Valid @ModelAttribute request: InternalAccountBootstrapRequest,
    ): List<InternalAccountBootstrapResponse> =
        queryService
            .findBootstrapBatch(request.toQuery())
            .map { account -> account.toBootstrapResponse() }
}
