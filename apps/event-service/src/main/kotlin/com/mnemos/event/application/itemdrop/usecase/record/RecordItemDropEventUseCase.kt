package com.mnemos.event.application.itemdrop.usecase.record

interface RecordItemDropEventUseCase {
    fun execute(command: RecordItemDropEventCommand): RecordItemDropResult
}
