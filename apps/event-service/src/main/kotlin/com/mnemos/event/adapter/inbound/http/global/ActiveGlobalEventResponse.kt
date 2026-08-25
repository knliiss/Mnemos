package com.mnemos.event.adapter.inbound.http.global

import com.mnemos.event.domain.global.model.DomainGlobalEventType
import java.time.Instant
import java.util.UUID

data class ActiveGlobalEventResponse(
    val id: UUID,
    val type: DomainGlobalEventType,
    val startedAt: Instant,
    val endsAt: Instant,
)
