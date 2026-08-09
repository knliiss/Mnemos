package com.mnemos.event.application.collector.usecase.submit

interface SubmitEventReportUseCase {
    fun execute(command: SubmitEventReportCommand): SubmitEventReportResult
}
