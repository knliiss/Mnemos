package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.social.contracts.event.DropSpotlightPublishedV1
import java.time.Instant
import java.util.UUID

data class RealtimeBroadcastResponse(
    val id: UUID,
    val type: String,
    val sequence: Long,
    val occurredAt: Instant,
    val expiresAt: Instant,
    val presentationMode: DropSpotlightPresentationMode,
    val payload: DropSpotlightPublishedV1,
)
