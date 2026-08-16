package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.adapter.inbound.http.profile.PublicProfileApiMapper.toResponse
import com.mnemos.profile.application.profile.PublicProfileService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/profiles")
class PublicProfileController(
    private val service: PublicProfileService,
) {
    @GetMapping("/{accountId}")
    fun get(@PathVariable accountId: UUID): PublicProfileResponse = service.get(accountId).toResponse()

    @GetMapping
    fun search(
        @RequestParam("q") query: String,
        pageable: Pageable,
    ): Page<PublicProfileResponse> =
        service
            .search(query, pageable)
            .map { profile -> profile.toResponse() }
}
