package com.mnemos.event.adapter.inbound.http.internal

import java.time.Instant
import java.util.UUID

data class InternalWipeResponse(
    val id: UUID,
    val startedAt: Instant,
    val endedAt: Instant?,
)
