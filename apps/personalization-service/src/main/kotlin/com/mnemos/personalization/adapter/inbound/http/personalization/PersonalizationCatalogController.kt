package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.adapter.inbound.http.common.PageResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationApiMapper.toResponse
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.personalization.catalog.ListPersonalizationsQuery
import com.mnemos.personalization.application.personalization.catalog.PersonalizationCatalogService
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations")
class PersonalizationCatalogController(
    private val catalogService: PersonalizationCatalogService,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListPersonalizationsRequest,
    ): PageResponse<PersonalizationResponse> =
        catalogService
            .list(
                ListPersonalizationsQuery(
                    type = request.type,
                    currency = request.currency,
                    page =
                        PageQuery(
                            page = request.page,
                            size = request.size,
                        ),
                ),
            ).toResponse()

    @GetMapping("/{personalizationId}")
    fun get(
        @PathVariable personalizationId: UUID,
    ): PersonalizationResponse =
        catalogService
            .get(PersonalizationId(personalizationId))
            .toResponse()
}
