package com.mnemos.personalization.adapter.inbound.http.custom

import com.mnemos.personalization.adapter.inbound.http.common.PageResponse
import com.mnemos.personalization.adapter.inbound.http.creator.CreatorPersonalizationApiMapper.toContent
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationApiMapper.toResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationResponse
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.personalization.usecase.createcustompack.CreateCustomNotificationPackCommand
import com.mnemos.personalization.application.personalization.usecase.createcustompack.CreateCustomNotificationPackUseCase
import com.mnemos.personalization.application.personalization.usecase.listcustompacks.ListCustomNotificationPacksUseCase
import com.mnemos.personalization.application.personalization.usecase.updatecustompack.UpdateCustomNotificationPackCommand
import com.mnemos.personalization.application.personalization.usecase.updatecustompack.UpdateCustomNotificationPackUseCase
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/custom-notification-packs")
class CustomNotificationPackController(
    private val createCustomNotificationPackUseCase: CreateCustomNotificationPackUseCase,
    private val listCustomNotificationPacksUseCase: ListCustomNotificationPacksUseCase,
    private val updateCustomNotificationPackUseCase: UpdateCustomNotificationPackUseCase,
) {
    @GetMapping("/me")
    fun listMine(
        @Valid
        @ModelAttribute
        request: ListCustomNotificationPacksRequest,
    ): PageResponse<PersonalizationResponse> =
        listCustomNotificationPacksUseCase
            .execute(
                PageQuery(
                    page = request.page,
                    size = request.size,
                ),
            ).toResponse()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody
        @Valid
        request: CreateCustomNotificationPackRequest,
    ): CreateCustomNotificationPackResponse {
        val result =
            createCustomNotificationPackUseCase.execute(
                CreateCustomNotificationPackCommand(
                    name = request.name,
                    content =
                        request.templates.toContent(
                            mixedAggregateTitle = request.mixedAggregateTitle,
                            valueMappings = request.valueMappings,
                        ),
                ),
            )

        return CreateCustomNotificationPackResponse(
            personalization = result.personalization.toResponse(),
            grantedForFree = result.grantedForFree,
        )
    }

    @PutMapping("/{personalizationId}")
    fun update(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: CreateCustomNotificationPackRequest,
    ): PersonalizationResponse =
        updateCustomNotificationPackUseCase
            .execute(
                UpdateCustomNotificationPackCommand(
                    personalizationId = PersonalizationId(personalizationId),
                    name = request.name,
                    content =
                        request.templates.toContent(
                            mixedAggregateTitle = request.mixedAggregateTitle,
                            valueMappings = request.valueMappings,
                        ),
                ),
            ).toResponse()
}
