package com.mnemos.profile.adapter.inbound.http.profile

import java.time.Instant
import java.util.UUID

data class PublicProfileResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val appearance: ProfileAppearanceResponse,
    val presence: PublicProfilePresenceResponse,
    val createdAt: Instant,
)
