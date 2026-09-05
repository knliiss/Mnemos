package com.mnemos.event.adapter.inbound.http.localization

import com.mnemos.event.adapter.inbound.http.localization.SaoLocalizationApiMapper.toResponse
import com.mnemos.event.adapter.inbound.http.localization.SaoLocalizationApiMapper.toSummaryResponse
import com.mnemos.event.application.localization.service.SaoLocalizationCatalogService
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Validated
@RestController
@RequestMapping("/api/v1/events/localizations/sao")
class SaoLocalizationController(
    private val catalogService: SaoLocalizationCatalogService,
) {
    @GetMapping
    fun list(): List<SaoLocalizationPackSummaryResponse> = catalogService.list().map { pack -> pack.toSummaryResponse() }

    @GetMapping("/{locale}")
    fun get(
        @PathVariable
        @Pattern(regexp = "^[a-z]{2}_[A-Z]{2}$")
        locale: String,
    ): SaoLocalizationPackResponse =
        catalogService.find(locale)?.toResponse()
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "SAO localization is not available for locale $locale",
            )
}
