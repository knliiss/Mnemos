package com.mnemos.bet.application.eventbet.usecase.create

interface CreateEventBetRoundUseCase {
    fun execute(command: CreateEventBetRoundCommand): CreateEventBetRoundResult
}
