package com.mnemos.bet.application.eventbet.usecase.resolve

import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId

data class ResolveEventBetRoundCommand(
    val roundId: EventBetRoundId,
    val outcome: EventBetOutcome,
    val reason: String,
)
