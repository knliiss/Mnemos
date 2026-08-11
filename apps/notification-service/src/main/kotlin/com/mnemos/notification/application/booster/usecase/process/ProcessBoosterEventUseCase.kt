package com.mnemos.notification.application.booster.usecase.process

interface ProcessBoosterEventUseCase {
    fun execute(command: ProcessBoosterEventCommand)
}
