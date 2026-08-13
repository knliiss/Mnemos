package com.mnemos.bet.application.eventbet.model

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import java.math.BigDecimal
import java.time.Instant

data class EventBetRoundView(
    val id: EventBetRoundId,
    val eventType: DomainBetGlobalEventType,
    val scheduledAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val yesOdds: BigDecimal,
    val noOdds: BigDecimal,
    val status: EventBetRoundStatus,
    val effectiveOutcome: EventBetOutcome?,
)
