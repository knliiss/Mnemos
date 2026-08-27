package com.mnemos.notification.application.playerpresence.usecase.process

fun interface ProcessPlayerWentOfflineEventUseCase {
    fun execute(command: ProcessPlayerWentOfflineEventCommand)
}
