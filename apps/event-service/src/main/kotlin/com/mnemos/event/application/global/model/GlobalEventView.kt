package com.mnemos.event.application.global.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import java.time.Instant

data class GlobalEventView(
    val id: EventId,
    val type: DomainGlobalEventType,
    val startedAt: Instant,
)
