package com.mnemos.personalization.adapter.inbound.http.ownership

import com.mnemos.personalization.adapter.inbound.http.common.PageResponse
import com.mnemos.personalization.adapter.inbound.http.ownership.PersonalizationOwnershipApiMapper.toResponse
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.ownership.PersonalizationOwnershipService
import com.mnemos.personalization.application.ownership.query.ListOwnedPersonalizationsQuery
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/ownership")
class PersonalizationOwnershipController(
    private val ownershipService: PersonalizationOwnershipService,
) {
    @GetMapping("/me")
    fun listMine(
        @Valid
        @ModelAttribute
        request: ListOwnedPersonalizationsRequest,
    ): PageResponse<OwnedPersonalizationResponse> =
        ownershipService
            .listMine(
                ListOwnedPersonalizationsQuery(
                    type = request.type,
                    page =
                        PageQuery(
                            page = request.page,
                            size = request.size,
                        ),
                ),
            ).toResponse()

    @PutMapping("/{personalizationId}/active")
    fun activate(
        @PathVariable personalizationId: UUID,
    ): OwnedPersonalizationResponse =
        ownershipService
            .activate(PersonalizationId(personalizationId))
            .toResponse()

    @DeleteMapping("/{personalizationId}/active")
    fun deactivate(
        @PathVariable personalizationId: UUID,
    ): OwnedPersonalizationResponse =
        ownershipService
            .deactivate(PersonalizationId(personalizationId))
            .toResponse()
}
