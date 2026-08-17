package com.mnemos.realtime.application.broadcast.model

import java.time.Instant
import java.util.UUID

data class RealtimeBroadcast(
    val id: UUID,
    val sequence: Long,
    val occurredAt: Instant,
    val expiresAt: Instant,
    val payload: DropSpotlightBroadcastPayload,
)
