package com.mnemos.event.domain.global.model

import com.mnemos.event.domain.EventId
import java.time.Instant

class GlobalEvent(
    val id: EventId = EventId.generate(),
    val type: DomainGlobalEventType,
    val startedAt: Instant,
)
