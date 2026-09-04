package com.mnemos.gateway.moderation

import java.time.Instant
import java.util.UUID

data class ActiveBanResponse(
    val sanctionId: UUID,
    val userId: UUID,
    val reason: String,
    val occurredAt: Instant,
    val expiresAt: Instant?,
)
