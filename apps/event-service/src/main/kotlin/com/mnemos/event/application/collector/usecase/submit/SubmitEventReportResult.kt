package com.mnemos.event.application.collector.usecase.submit

import com.mnemos.event.domain.verification.model.EventCandidateId

data class SubmitEventReportResult(
    val candidateId: EventCandidateId,
    val status: SubmitEventReportStatus,
    val confirmations: Int,
    val requiredConfirmations: Int,
)
