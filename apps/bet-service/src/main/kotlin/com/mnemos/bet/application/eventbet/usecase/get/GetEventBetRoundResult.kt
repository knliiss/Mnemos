package com.mnemos.bet.application.eventbet.usecase.get

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView

data class GetEventBetRoundResult(
    val round: EventBetRoundView,
    val myBet: EventBetView?,
)
