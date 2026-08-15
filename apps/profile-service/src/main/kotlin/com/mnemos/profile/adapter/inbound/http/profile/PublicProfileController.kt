package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.application.profile.PublicProfileService
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class PublicProfileResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val createdAt: Instant,
)

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
    ): Page<PublicProfileResponse> = service.search(query, pageable).map { it.toResponse() }

    private fun PublicProfile.toResponse(): PublicProfileResponse =
        PublicProfileResponse(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            createdAt = createdAt,
        )
}
