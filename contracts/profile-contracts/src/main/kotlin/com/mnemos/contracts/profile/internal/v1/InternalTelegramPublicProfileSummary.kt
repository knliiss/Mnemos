package com.mnemos.contracts.profile.internal.v1

import java.time.Instant
import java.util.UUID

data class InternalTelegramPublicProfileSummary(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val nickname: String,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val presenceStatus: String,
    val realm: String?,
    val lastSeenOnline: Instant?,
    val createdAt: Instant,
)
