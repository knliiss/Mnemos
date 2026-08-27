package com.mnemos.playerdirectory.application.tracking.usecase.activity

fun interface RecordPlayerDropActivityUseCase {
    fun execute(command: RecordPlayerDropActivityCommand)
}
