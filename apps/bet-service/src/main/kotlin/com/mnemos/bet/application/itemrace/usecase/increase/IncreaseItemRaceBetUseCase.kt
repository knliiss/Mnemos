package com.mnemos.bet.application.itemrace.usecase.increase

interface IncreaseItemRaceBetUseCase {
    fun execute(command: IncreaseItemRaceBetCommand): IncreaseItemRaceBetResult
}
