package com.mnemos.bet.application.eventbet.usecase.increase

interface IncreaseEventBetUseCase {
    fun execute(command: IncreaseEventBetCommand): IncreaseEventBetResult
}
