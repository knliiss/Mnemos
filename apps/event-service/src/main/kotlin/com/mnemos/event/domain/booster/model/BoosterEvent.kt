package com.mnemos.event.domain.booster.model

import com.mnemos.event.domain.EventId
import java.time.Instant

class BoosterEvent(
    val id: EventId = EventId.generate(),
    val type: DomainBoosterType,
    val activatedBy: String,
    val activatedAt: Instant,
)
