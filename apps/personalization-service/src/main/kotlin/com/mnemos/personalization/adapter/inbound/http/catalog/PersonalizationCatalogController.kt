package com.mnemos.personalization.adapter.inbound.http.catalog

import com.mnemos.personalization.adapter.inbound.http.common.PageResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationApiMapper.toResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationResponse
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.personalization.usecase.getcatalog.GetPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.listcatalog.ListPersonalizationsQuery
import com.mnemos.personalization.application.personalization.usecase.listcatalog.ListPersonalizationsUseCase
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
    private val listPersonalizationsUseCase: ListPersonalizationsUseCase,
    private val getPersonalizationUseCase: GetPersonalizationUseCase,
) {
    @GetMapping
    fun list(
        @Valid
        @ModelAttribute
        request: ListPersonalizationsRequest,
    ): PageResponse<PersonalizationResponse> =
        listPersonalizationsUseCase
            .execute(
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
        getPersonalizationUseCase
            .execute(PersonalizationId(personalizationId))
            .toResponse()
}
