package com.mnemos.event.application.raid.usecase.record

interface RecordRaidEventUseCase {
    fun execute(command: RecordRaidEventCommand): RecordRaidEventResult
}
