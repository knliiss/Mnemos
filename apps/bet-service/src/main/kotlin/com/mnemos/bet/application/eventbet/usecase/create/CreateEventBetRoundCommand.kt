package com.mnemos.bet.application.eventbet.usecase.create

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import java.time.Instant

data class CreateEventBetRoundCommand(
    val eventType: DomainBetGlobalEventType,
    val scheduledAt: Instant,
)
