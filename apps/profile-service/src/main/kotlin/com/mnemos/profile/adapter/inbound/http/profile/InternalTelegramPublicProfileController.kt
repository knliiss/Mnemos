package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.contracts.profile.internal.v1.InternalTelegramProfileSearchPage
import com.mnemos.contracts.profile.internal.v1.InternalTelegramPublicProfileSummary
import com.mnemos.profile.adapter.inbound.http.profile.InternalTelegramProfileMapper.toInternalSummary
import com.mnemos.profile.adapter.inbound.http.profile.PublicProfileApiMapper.toQuery
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
@RequestMapping("/internal/v1/telegram/profiles")
class InternalTelegramPublicProfileController(
    private val getPublicProfile: GetPublicProfileUseCase,
    private val searchPublicProfiles: SearchPublicProfilesUseCase,
) {
    @GetMapping
    fun search(@Valid @ModelAttribute request: SearchPublicProfilesRequest): InternalTelegramProfileSearchPage {
        val result = searchPublicProfiles.execute(request.toQuery())
        return InternalTelegramProfileSearchPage(
            items = result.items.map { it.toInternalSummary() },
            page = result.page,
            size = result.size,
            totalItems = result.totalItems,
            totalPages = result.totalPages,
            hasNextPage = result.hasNextPage,
        )
    }

    @GetMapping("/{accountId}")
    fun get(@PathVariable accountId: UUID): InternalTelegramPublicProfileSummary =
        getPublicProfile.execute(accountId).toInternalSummary()
}
