package com.mnemos.event.adapter.out.persistence.global.query

import com.mnemos.event.domain.global.model.DomainGlobalEventType
import java.time.Instant
import java.util.UUID

data class GlobalEventViewProjection(
    val id: UUID,
    val type: DomainGlobalEventType,
    val startedAt: Instant,
)
