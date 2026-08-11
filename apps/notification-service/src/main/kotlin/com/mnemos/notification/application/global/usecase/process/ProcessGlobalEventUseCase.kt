package com.mnemos.notification.application.global.usecase.process

interface ProcessGlobalEventUseCase {
    fun execute(command: ProcessGlobalEventCommand)
}
