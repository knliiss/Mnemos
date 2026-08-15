package com.mnemos.realtime.application.broadcast.model

import com.mnemos.social.contracts.event.DropSpotlightPublishedV1
import java.time.Instant
import java.util.UUID

data class RealtimeBroadcast(
    val id: UUID,
    val sequence: Long,
    val occurredAt: Instant,
    val expiresAt: Instant,
    val payload: DropSpotlightPublishedV1,
)
