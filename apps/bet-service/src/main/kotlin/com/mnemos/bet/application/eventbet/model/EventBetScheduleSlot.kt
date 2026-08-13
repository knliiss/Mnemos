package com.mnemos.bet.application.eventbet.model

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import java.time.Instant

data class EventBetScheduleSlot(
    val eventType: DomainBetGlobalEventType,
    val scheduledAt: Instant,
)
