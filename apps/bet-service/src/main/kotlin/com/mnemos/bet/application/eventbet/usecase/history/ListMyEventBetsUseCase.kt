package com.mnemos.bet.application.eventbet.usecase.history

import com.mnemos.bet.application.eventbet.model.EventBetView

interface ListMyEventBetsUseCase {
    fun execute(): List<EventBetView>
}
