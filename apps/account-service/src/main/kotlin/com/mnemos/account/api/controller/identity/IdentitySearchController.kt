package com.mnemos.account.api.controller.identity

import com.mnemos.account.api.dto.identity.IdentitySearchApiMapper.toQuery
import com.mnemos.account.api.dto.identity.IdentitySearchApiMapper.toResponse
import com.mnemos.account.api.dto.identity.IdentitySearchRequest
import com.mnemos.account.api.dto.identity.IdentitySearchResponse
import com.mnemos.account.application.service.identity.IdentitySearchService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/identities")
class IdentitySearchController(
    private val identitySearchService: IdentitySearchService,
) {
    @GetMapping
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
    fun search(
        @Valid @ModelAttribute request: IdentitySearchRequest,
    ): List<IdentitySearchResponse> =
        identitySearchService
            .search(request.toQuery())
            .map { result -> result.toResponse() }
}
