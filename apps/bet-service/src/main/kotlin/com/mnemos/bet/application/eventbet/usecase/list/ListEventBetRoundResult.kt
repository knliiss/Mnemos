package com.mnemos.bet.application.eventbet.usecase.list

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView

data class ListEventBetRoundResult(
    val round: EventBetRoundView,
    val myBet: EventBetView?,
)
