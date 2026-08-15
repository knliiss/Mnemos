package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationApiMapper.toResponse
import com.mnemos.personalization.application.personalization.catalog.ListPersonalizationsQuery
import com.mnemos.personalization.application.personalization.catalog.PersonalizationCatalogService
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations")
class PersonalizationCatalogController(
    private val catalogService: PersonalizationCatalogService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) type: PersonalizationType?,
        @RequestParam(required = false) currency: Currency?,
    ): List<PersonalizationResponse> =
        catalogService
            .list(
                ListPersonalizationsQuery(
                    type = type,
                    currency = currency,
                ),
            ).map { personalization -> personalization.toResponse() }

    @GetMapping("/{personalizationId}")
    fun get(
        @PathVariable personalizationId: UUID,
    ): PersonalizationResponse =
        catalogService
            .get(PersonalizationId(personalizationId))
            .toResponse()
}
