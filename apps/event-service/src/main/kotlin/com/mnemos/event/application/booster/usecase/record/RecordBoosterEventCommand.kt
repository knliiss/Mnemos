package com.mnemos.event.application.booster.usecase.record

import com.mnemos.event.domain.booster.model.DomainBoosterType
import java.time.Instant

data class RecordBoosterEventCommand(
    val type: DomainBoosterType,
    val activatedBy: String,
    val activatedAt: Instant,
)
