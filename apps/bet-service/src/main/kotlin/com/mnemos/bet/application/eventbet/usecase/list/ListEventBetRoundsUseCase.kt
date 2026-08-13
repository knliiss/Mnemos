package com.mnemos.bet.application.eventbet.usecase.list

import com.mnemos.bet.application.eventbet.model.EventBetRoundView

interface ListEventBetRoundsUseCase {
    fun execute(): List<EventBetRoundView>
}
