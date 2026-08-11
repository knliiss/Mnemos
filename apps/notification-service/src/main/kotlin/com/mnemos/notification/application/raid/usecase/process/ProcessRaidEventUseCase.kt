package com.mnemos.notification.application.raid.usecase.process

interface ProcessRaidEventUseCase {
    fun execute(command: ProcessRaidEventCommand)
}
