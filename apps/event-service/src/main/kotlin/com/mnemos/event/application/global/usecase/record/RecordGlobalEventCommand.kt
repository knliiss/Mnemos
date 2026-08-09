package com.mnemos.event.application.global.usecase.record

import com.mnemos.event.domain.global.model.DomainGlobalEventType
import java.time.Instant

data class RecordGlobalEventCommand(
    val type: DomainGlobalEventType,
    val startedAt: Instant,
)
