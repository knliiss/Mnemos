package com.mnemos.bet.application.eventbet.usecase.place

interface PlaceEventBetUseCase {
    fun execute(command: PlaceEventBetCommand): PlaceEventBetResult
}
