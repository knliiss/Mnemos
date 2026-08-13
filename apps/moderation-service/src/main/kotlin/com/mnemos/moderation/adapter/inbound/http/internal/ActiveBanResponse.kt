package com.mnemos.moderation.adapter.inbound.http.internal

import java.time.Instant
import java.util.UUID

data class ActiveBanResponse(
    val sanctionId: UUID,
    val userId: UUID,
    val occurredAt: Instant,
)
