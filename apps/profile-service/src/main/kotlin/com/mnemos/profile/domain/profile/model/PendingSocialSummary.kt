package com.mnemos.profile.domain.profile.model

import java.time.Instant
import java.util.UUID

data class PendingSocialSummary(
    val accountId: UUID,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val revision: Long,
    val occurredAt: Instant,
)
