package com.mnemos.bet.application.itemrace.usecase.create

interface CreateItemRaceUseCase {
    fun execute(command: CreateItemRaceCommand): CreateItemRaceResult
}
