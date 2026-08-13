package com.mnemos.bet.application.eventbet.usecase.get

interface GetEventBetRoundUseCase {
    fun execute(query: GetEventBetRoundQuery): GetEventBetRoundResult
}
