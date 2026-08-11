package com.mnemos.notification.application.dungeon.usecase.process

interface ProcessDungeonEventUseCase {
    fun execute(command: ProcessDungeonEventCommand)
}
