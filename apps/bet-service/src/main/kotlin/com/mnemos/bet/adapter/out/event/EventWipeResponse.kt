package com.mnemos.bet.adapter.out.event

import java.time.Instant
import java.util.UUID

data class EventWipeResponse(
    val id: UUID,
    val startedAt: Instant,
    val endedAt: Instant?,
)
