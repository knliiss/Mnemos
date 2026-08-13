package com.mnemos.bet.application.eventbet.usecase.get

import com.mnemos.bet.domain.eventbet.model.EventBetRoundId

data class GetEventBetRoundQuery(
    val roundId: EventBetRoundId,
)
