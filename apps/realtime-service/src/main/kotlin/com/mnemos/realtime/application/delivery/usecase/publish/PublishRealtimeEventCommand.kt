package com.mnemos.realtime.application.delivery.usecase.publish

import com.mnemos.realtime.application.delivery.model.RealtimeEventPayload
import java.time.Instant
import java.util.UUID

data class PublishRealtimeEventCommand(
    val eventId: UUID,
    val userId: UUID,
    val occurredAt: Instant,
    val operationId: UUID?,
    val payload: RealtimeEventPayload,
)
