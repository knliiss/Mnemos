package com.mnemos.bet.domain.occurrence.model

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import java.time.Instant

class GlobalOccurrence(
    val eventId: ExternalEventId,
    val eventType: DomainBetGlobalEventType,
    val occurredAt: Instant,
)
