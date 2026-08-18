package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.model.GameEventBroadcastPayload
import java.time.Instant
import java.util.UUID

data class RealtimeGameEventResponse(
    val id: UUID,
    val type: String,
    val occurredAt: Instant,
    val payload: GameEventBroadcastPayload,
)
