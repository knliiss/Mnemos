package com.mnemos.event.adapter.out.persistence.raid.query

import java.time.Instant
import java.util.UUID

data class RaidEventViewProjection(
    val id: UUID,
    val locations: List<Int>,
    val startedAt: Instant,
)
