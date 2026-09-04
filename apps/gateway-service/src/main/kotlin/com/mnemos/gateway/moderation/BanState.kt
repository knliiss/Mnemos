package com.mnemos.gateway.moderation

import java.time.Instant
import java.util.UUID

data class BanState(
    val sanctionId: UUID,
    val reason: String,
    val occurredAt: Instant,
    val expiresAt: Instant?,
)
