package com.mnemos.telegram.bot.application.profile.model

import java.time.Instant
import java.util.UUID

data class PublicProfileSummary(
    val accountId: UUID,
    val nickname: String,
    val cristalixUuid: UUID,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val presenceStatus: ProfilePresenceStatus,
    val realm: String?,
    val lastSeenOnline: Instant?,
    val createdAt: Instant,
)
