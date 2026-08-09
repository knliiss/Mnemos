package com.mnemos.event.application.booster.usecase.record

interface RecordBoosterEventUseCase {
    fun execute(command: RecordBoosterEventCommand): RecordBoosterEventResult
}
