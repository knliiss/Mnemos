package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.domain.profile.model.PublicProfile

object PublicProfileApiMapper {
    fun PublicProfile.toResponse(): PublicProfileResponse =
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
