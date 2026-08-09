package com.mnemos.event.application.raid.model

import com.mnemos.event.domain.EventId
import java.time.Instant

data class RaidEventView(
    val id: EventId,
    val locations: List<Int>,
    val startedAt: Instant,
)
