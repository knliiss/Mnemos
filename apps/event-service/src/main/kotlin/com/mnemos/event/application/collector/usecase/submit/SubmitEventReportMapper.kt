package com.mnemos.event.application.collector.usecase.submit

import com.mnemos.event.domain.verification.model.EventCandidate
import com.mnemos.event.domain.verification.model.EventCandidateAcceptResult

object SubmitEventReportMapper {
    fun EventCandidateAcceptResult.toSubmitStatus(): SubmitEventReportStatus =
        when (this) {
            EventCandidateAcceptResult.ACCEPTED,
            EventCandidateAcceptResult.CONFIRMED,
            EventCandidateAcceptResult.ALREADY_CONFIRMED,
            ->
                SubmitEventReportStatus.ACCEPTED

            EventCandidateAcceptResult.DUPLICATE ->
                SubmitEventReportStatus.DUPLICATE

            EventCandidateAcceptResult.ALREADY_RECORDED ->
                SubmitEventReportStatus.ALREADY_RECORDED
        }

    fun EventCandidate.toSubmitResult(status: SubmitEventReportStatus): SubmitEventReportResult =
        SubmitEventReportResult(
            candidateId = id,
            status = status,
            confirmations = confirmationCount,
            requiredConfirmations = requiredConfirmations,
        )
}
