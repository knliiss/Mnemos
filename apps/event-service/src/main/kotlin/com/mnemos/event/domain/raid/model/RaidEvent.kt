package com.mnemos.event.domain.raid.model

import com.mnemos.event.domain.EventId
import java.time.Instant

data class RaidEvent(
    val id: EventId = EventId.generate(),
    val locations: Set<Int>,
    val startedAt: Instant,
)
