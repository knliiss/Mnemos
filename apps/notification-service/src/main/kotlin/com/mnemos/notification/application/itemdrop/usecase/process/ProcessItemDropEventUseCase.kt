package com.mnemos.notification.application.itemdrop.usecase.process

interface ProcessItemDropEventUseCase {
    fun execute(command: ProcessItemDropEventCommand)
}
