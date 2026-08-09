package com.mnemos.event.adapter.inbound.http.booster

import com.mnemos.event.domain.booster.model.DomainBoosterType
import java.time.Instant
import java.util.UUID

data class BoosterEventResponse(
    val id: UUID,
    val type: DomainBoosterType,
    val activatedBy: String,
    val activatedAt: Instant,
)
