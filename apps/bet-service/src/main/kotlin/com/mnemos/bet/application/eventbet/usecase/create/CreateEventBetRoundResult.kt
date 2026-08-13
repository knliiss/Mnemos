package com.mnemos.bet.application.eventbet.usecase.create

import com.mnemos.bet.domain.eventbet.model.EventBetRoundId

data class CreateEventBetRoundResult(
    val roundId: EventBetRoundId,
    val created: Boolean,
)
