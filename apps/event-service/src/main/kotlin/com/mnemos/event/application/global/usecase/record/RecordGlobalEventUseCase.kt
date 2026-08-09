package com.mnemos.event.application.global.usecase.record

interface RecordGlobalEventUseCase {
    fun execute(command: RecordGlobalEventCommand): RecordGlobalEventResult
}
