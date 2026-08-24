package com.mnemos.event.adapter.inbound.http.wipe

import java.time.Instant
import java.util.UUID

data class WipeResponse(
    val id: UUID,
    val number: Int,
    val name: String?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val active: Boolean,
    val createdBy: String,
    val createdAt: Instant,
)
