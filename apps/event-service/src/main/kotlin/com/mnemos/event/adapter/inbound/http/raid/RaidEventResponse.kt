package com.mnemos.event.adapter.inbound.http.raid

import java.time.Instant
import java.util.UUID

data class RaidEventResponse(
    val id: UUID,
    val locations: List<Int>,
    val startedAt: Instant,
)
