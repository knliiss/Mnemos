package com.mnemos.event.domain.verification.model

import java.time.Instant
import java.util.UUID

data class EventObservation(
    val collectorId: CollectorId,
    val messageId: UUID,
    val observedAt: Instant,
    val receivedAt: Instant,
)
