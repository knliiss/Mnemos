package com.mnemos.bet.adapter.inbound.http.eventbet

import com.mnemos.bet.contracts.shared.BetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class EventBetRoundResponse(
    val id: UUID,
    val eventType: BetGlobalEventType,
    val scheduledAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val yesOdds: BigDecimal,
    val noOdds: BigDecimal,
    val status: EventBetRoundStatus,
    val outcome: EventBetOutcome?,
    val myBet: EventBetResponse?,
)
