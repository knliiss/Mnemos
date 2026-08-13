package com.mnemos.bet.application.eventoccurrence.usecase.record

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.occurrence.model.ExternalEventId
import java.time.Instant

data class RecordGlobalOccurrenceCommand(
    val eventId: ExternalEventId,
    val eventType: DomainBetGlobalEventType,
    val occurredAt: Instant,
)
