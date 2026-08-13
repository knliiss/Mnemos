package com.mnemos.bet.application.eventbet.usecase.increase

import com.mnemos.bet.domain.eventbet.model.EventBetId

data class IncreaseEventBetResult(
    val betId: EventBetId,
)
