package com.mnemos.event.adapter.inbound.http.localization

import com.mnemos.event.adapter.inbound.http.localization.SaoLocalizationApiMapper.toResponse
import com.mnemos.event.application.localization.service.SaoLocalizationCatalogService
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Validated
@RestController
@RequestMapping("/internal/v1/events/localizations/sao")
@PreAuthorize("authentication.name == 'notification-service' || authentication.name == 'telegram-bot-service'")
class InternalSaoLocalizationController(
    private val catalogService: SaoLocalizationCatalogService,
) {
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
