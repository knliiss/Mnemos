package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.creator.CreatorPersonalizationApiMapper.toCreateCommand
import com.mnemos.personalization.adapter.inbound.http.creator.CreatorPersonalizationApiMapper.toUpdateCommand
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateAvatarFrameRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateNickStyleRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateNotificationPackRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateProfileEffectRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateProfileThemeRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationApiMapper.toResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationResponse
import com.mnemos.personalization.application.personalization.usecase.createcreator.CreateCreatorPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.listcreator.ListCreatorPersonalizationsUseCase
import com.mnemos.personalization.application.personalization.usecase.publish.PublishPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.submit.SubmitPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.updatecreator.UpdateCreatorPersonalizationUseCase
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/creator")
@PreAuthorize("hasAnyRole('PERSONALIZATION_CREATOR', 'ADMIN', 'SUPER_ADMIN')")
class CreatorPersonalizationController(
    private val createCreatorPersonalizationUseCase: CreateCreatorPersonalizationUseCase,
    private val updateCreatorPersonalizationUseCase: UpdateCreatorPersonalizationUseCase,
    private val listCreatorPersonalizationsUseCase: ListCreatorPersonalizationsUseCase,
    private val submitPersonalizationUseCase: SubmitPersonalizationUseCase,
    private val publishPersonalizationUseCase: PublishPersonalizationUseCase,
) {
    @GetMapping("/me")
    fun listMine(): List<PersonalizationResponse> =
        listCreatorPersonalizationsUseCase.execute().map { personalization -> personalization.toResponse() }

    @PostMapping("/nick-styles")
    @ResponseStatus(HttpStatus.CREATED)
    fun createNickStyle(
        @RequestBody
        @Valid
        request: CreateNickStyleRequest,
    ): PersonalizationResponse = createCreatorPersonalizationUseCase.execute(request.toCreateCommand()).toResponse()

    @PostMapping("/profile-themes")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProfileTheme(
        @RequestBody
        @Valid
        request: CreateProfileThemeRequest,
    ): PersonalizationResponse = createCreatorPersonalizationUseCase.execute(request.toCreateCommand()).toResponse()

    @PostMapping("/avatar-frames")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAvatarFrame(
        @RequestBody
        @Valid
        request: CreateAvatarFrameRequest,
    ): PersonalizationResponse = createCreatorPersonalizationUseCase.execute(request.toCreateCommand()).toResponse()

    @PostMapping("/profile-effects")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProfileEffect(
        @RequestBody
        @Valid
        request: CreateProfileEffectRequest,
    ): PersonalizationResponse = createCreatorPersonalizationUseCase.execute(request.toCreateCommand()).toResponse()

    @PostMapping("/notification-packs")
    @ResponseStatus(HttpStatus.CREATED)
    fun createNotificationPack(
        @RequestBody
        @Valid
        request: CreateNotificationPackRequest,
    ): PersonalizationResponse = createCreatorPersonalizationUseCase.execute(request.toCreateCommand()).toResponse()

    @PutMapping("/{personalizationId}/nick-style")
    fun updateNickStyle(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: CreateNickStyleRequest,
    ): PersonalizationResponse =
        updateCreatorPersonalizationUseCase
            .execute(request.toUpdateCommand(PersonalizationId(personalizationId)))
            .toResponse()

    @PutMapping("/{personalizationId}/profile-theme")
    fun updateProfileTheme(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: CreateProfileThemeRequest,
    ): PersonalizationResponse =
        updateCreatorPersonalizationUseCase
            .execute(request.toUpdateCommand(PersonalizationId(personalizationId)))
            .toResponse()

    @PutMapping("/{personalizationId}/avatar-frame")
    fun updateAvatarFrame(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: CreateAvatarFrameRequest,
    ): PersonalizationResponse =
        updateCreatorPersonalizationUseCase
            .execute(request.toUpdateCommand(PersonalizationId(personalizationId)))
            .toResponse()

    @PutMapping("/{personalizationId}/profile-effect")
    fun updateProfileEffect(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: CreateProfileEffectRequest,
    ): PersonalizationResponse =
        updateCreatorPersonalizationUseCase
            .execute(request.toUpdateCommand(PersonalizationId(personalizationId)))
            .toResponse()

    @PutMapping("/{personalizationId}/notification-pack")
    fun updateNotificationPack(
        @PathVariable personalizationId: UUID,
        @RequestBody
        @Valid
        request: CreateNotificationPackRequest,
    ): PersonalizationResponse =
        updateCreatorPersonalizationUseCase
            .execute(request.toUpdateCommand(PersonalizationId(personalizationId)))
            .toResponse()

    @PostMapping("/{personalizationId}/submit")
    fun submit(
        @PathVariable personalizationId: UUID,
    ): PersonalizationResponse =
        submitPersonalizationUseCase
            .execute(PersonalizationId(personalizationId))
            .toResponse()

    @PostMapping("/{personalizationId}/publish")
    fun publish(
        @PathVariable personalizationId: UUID,
    ): PersonalizationResponse =
        publishPersonalizationUseCase
            .execute(PersonalizationId(personalizationId))
            .toResponse()
}
