package com.mnemos.profile.domain.profile.model

import java.time.Instant
import java.util.UUID

data class PublicProfile(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val socialRevision: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val appearance: ProfileAppearance = ProfileAppearance.empty(accountId),
    val playerState: ProfilePlayerState? = null,
)
