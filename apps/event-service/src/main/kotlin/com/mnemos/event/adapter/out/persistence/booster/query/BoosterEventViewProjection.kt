package com.mnemos.event.adapter.out.persistence.booster.query

import com.mnemos.event.domain.booster.model.DomainBoosterType
import java.time.Instant
import java.util.UUID

data class BoosterEventViewProjection(
    val id: UUID,
    val type: DomainBoosterType,
    val activatedBy: String,
    val activatedAt: Instant,
)
