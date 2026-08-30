package com.mnemos.event.contracts.internal.v1

import java.time.Instant
import java.util.UUID

data class InternalWipeResponse(
    val id: UUID,
    val number: Int,
    val name: String?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val active: Boolean,
)
