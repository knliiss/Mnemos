package com.mnemos.bet.application.eventbet.usecase.increase

import com.mnemos.bet.domain.eventbet.model.EventBetRoundId

data class IncreaseEventBetCommand(
    val roundId: EventBetRoundId,
    val amount: Long,
)
