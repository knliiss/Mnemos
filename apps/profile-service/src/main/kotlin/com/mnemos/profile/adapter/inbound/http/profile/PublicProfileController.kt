package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.profile.adapter.inbound.http.profile.PublicProfileApiMapper.toQuery
import com.mnemos.profile.adapter.inbound.http.profile.PublicProfileApiMapper.toResponse
import com.mnemos.profile.application.profile.usecase.get.GetPublicProfileUseCase
import com.mnemos.profile.application.profile.usecase.search.SearchPublicProfilesUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/profiles")
class PublicProfileController(
    private val getPublicProfileUseCase: GetPublicProfileUseCase,
    private val searchPublicProfilesUseCase: SearchPublicProfilesUseCase,
) {
    @GetMapping("/{accountId}")
    fun get(
        @PathVariable
        accountId: UUID,
    ): PublicProfileResponse = getPublicProfileUseCase.execute(accountId).toResponse()

    @GetMapping
    fun search(
        @Valid @ModelAttribute request: SearchPublicProfilesRequest,
    ): PageResponse<PublicProfileResponse> = searchPublicProfilesUseCase.execute(request.toQuery()).toResponse()
}
