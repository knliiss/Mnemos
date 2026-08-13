package com.mnemos.bet.application.itemrace.usecase.join

interface JoinItemRaceUseCase {
    fun execute(command: JoinItemRaceCommand): JoinItemRaceResult
}
