package com.mnemos.event.application.collector.usecase.submit

import com.mnemos.event.domain.verification.model.CollectorId
import com.mnemos.event.domain.verification.model.report.CollectorEventReport
import java.time.Instant
import java.util.UUID

data class SubmitEventReportCommand(
    val collectorId: CollectorId,
    val messageId: UUID,
    val observedAt: Instant,
    val receivedAt: Instant,
    val activeCollectorCount: Int,
    val report: CollectorEventReport,
) {
    init {
        require(activeCollectorCount > 0)
    }
}
