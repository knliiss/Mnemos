package com.mnemos.personalization.adapter.inbound.http.moderation

import com.mnemos.personalization.adapter.inbound.http.creator.CreatorPersonalizationApiMapper.toDomain
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationApiMapper.toResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationResponse
import com.mnemos.personalization.application.personalization.usecase.approve.ApprovePersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.listmanaged.ListManagedPersonalizationsUseCase
import com.mnemos.personalization.application.personalization.usecase.listreviewqueue.ListPersonalizationReviewQueueUseCase
import com.mnemos.personalization.application.personalization.usecase.reject.RejectPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.relist.RelistPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.unlist.UnlistPersonalizationUseCase
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/moderation")
@PreAuthorize("hasAnyRole('PERSONALIZATION_MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
class PersonalizationModerationController(
    private val listReviewQueueUseCase: ListPersonalizationReviewQueueUseCase,
    private val listManagedPersonalizationsUseCase: ListManagedPersonalizationsUseCase,
    private val approvePersonalizationUseCase: ApprovePersonalizationUseCase,
    private val rejectPersonalizationUseCase: RejectPersonalizationUseCase,
    private val unlistPersonalizationUseCase: UnlistPersonalizationUseCase,
    private val relistPersonalizationUseCase: RelistPersonalizationUseCase,
) {
    @GetMapping("/review")
    fun reviewQueue(): List<PersonalizationResponse> =
        listReviewQueueUseCase.execute().map { personalization -> personalization.toResponse() }

    @GetMapping("/managed")
    fun managed(): List<PersonalizationResponse> =
        listManagedPersonalizationsUseCase.execute().map { personalization -> personalization.toResponse() }

    @PostMapping("/{personalizationId}/approve")
    fun approve(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: ApprovePersonalizationRequest,
    ): PersonalizationResponse =
        approvePersonalizationUseCase
            .execute(
                personalizationId = PersonalizationId(personalizationId),
                price = request.price.toDomain(),
            ).toResponse()

    @PostMapping("/{personalizationId}/reject")
    fun reject(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: RejectPersonalizationRequest,
    ): PersonalizationResponse =
        rejectPersonalizationUseCase
            .execute(
                personalizationId = PersonalizationId(personalizationId),
                reason = request.reason,
            ).toResponse()

    @PostMapping("/{personalizationId}/unlist")
    fun unlist(
        @PathVariable personalizationId: UUID,
    ): PersonalizationResponse =
        unlistPersonalizationUseCase
            .execute(PersonalizationId(personalizationId))
            .toResponse()

    @PostMapping("/{personalizationId}/relist")
    fun relist(
        @PathVariable personalizationId: UUID,
    ): PersonalizationResponse =
        relistPersonalizationUseCase
            .execute(PersonalizationId(personalizationId))
            .toResponse()
}
