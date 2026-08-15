package com.mnemos.realtime.adapter.sse.protocol

import com.mnemos.realtime.application.delivery.model.RealtimeEventPayload
import java.time.Instant
import java.util.UUID

data class RealtimeEventResponse(
    val id: UUID,
    val type: String,
    val occurredAt: Instant,
    val operationId: UUID?,
    val payload: RealtimeEventPayload,
)
