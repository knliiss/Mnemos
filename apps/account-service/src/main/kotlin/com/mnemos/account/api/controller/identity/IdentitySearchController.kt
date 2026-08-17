package com.mnemos.account.api.controller.identity

import com.mnemos.account.api.dto.identity.IdentitySearchApiMapper.toResponse
import com.mnemos.account.api.dto.identity.IdentitySearchResponse
import com.mnemos.account.application.service.identity.IdentitySearchService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v1/admin/identities")
class IdentitySearchController(
    private val identitySearchService: IdentitySearchService,
) {
    @GetMapping
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
    fun search(
        @RequestParam
        @Size(min = 2, max = 64)
        query: String,
        @RequestParam(defaultValue = "20")
        @Min(1)
        @Max(50)
        limit: Int,
    ): List<IdentitySearchResponse> =
        identitySearchService
            .search(
                query = query,
                limit = limit,
            ).map { result -> result.toResponse() }
}
