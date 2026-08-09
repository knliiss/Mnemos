package com.mnemos.event.application.collector.usecase.submit

import com.mnemos.event.application.collector.port.out.ActiveCollectorProvider
import com.mnemos.event.application.collector.port.out.EventCandidateLock
import com.mnemos.event.application.collector.service.ConfirmedEventRecorder
import com.mnemos.event.application.collector.verification.EventConfirmationPolicy
import com.mnemos.event.configuration.collector.EventVerificationProperties
import com.mnemos.event.domain.verification.model.EventCandidate
import com.mnemos.event.domain.verification.model.EventCandidateAcceptResult
import com.mnemos.event.domain.verification.model.EventCandidateStatus
import com.mnemos.event.domain.verification.model.EventFingerprint
import com.mnemos.event.domain.verification.model.EventObservation
import com.mnemos.event.domain.verification.model.report.fingerprint
import com.mnemos.event.domain.verification.repository.EventCandidateRepository
import org.springframework.stereotype.Service

@Service
class SubmitEventReportService(
    private val candidateRepository: EventCandidateRepository,
    private val candidateLock: EventCandidateLock,
    private val activeCollectorProvider: ActiveCollectorProvider,
    private val confirmationPolicy: EventConfirmationPolicy,
    private val confirmedEventRecorder: ConfirmedEventRecorder,
    private val properties: EventVerificationProperties,
) : SubmitEventReportUseCase {
    override fun execute(command: SubmitEventReportCommand): SubmitEventReportResult {
        val fingerprint = command.report.fingerprint()

        val processingResult =
            candidateLock.withLock(fingerprint) {
                processReport(
                    command = command,
                    fingerprint = fingerprint,
                )
            }

        if (!processingResult.shouldRecord) {
            return processingResult.toResult()
        }

        return record(
            fingerprint = fingerprint,
            processingResult = processingResult,
        )
    }

    private fun processReport(
        command: SubmitEventReportCommand,
        fingerprint: EventFingerprint,
    ): ProcessingResult {
        val candidates =
            candidateRepository.findActiveByFingerprint(
                fingerprint = fingerprint,
                now = command.receivedAt,
            )

        val retryCandidate =
            candidates.firstOrNull {
                it.hasMessage(
                    collectorId = command.collectorId,
                    messageId = command.messageId,
                )
            }

        if (retryCandidate != null) {
            return processRetry(retryCandidate)
        }

        val candidate =
            candidates
                .asSequence()
                .filter {
                    it.matchesTime(
                        observedAt = command.observedAt,
                        matchingWindow = properties.matchingWindow,
                    )
                }.filterNot {
                    it.hasCollector(command.collectorId)
                }.minByOrNull {
                    it.referenceObservedAt
                }

        val observation =
            EventObservation(
                collectorId = command.collectorId,
                messageId = command.messageId,
                observedAt = command.observedAt,
                receivedAt = command.receivedAt,
            )

        if (candidate == null) {
            return createCandidate(
                command = command,
                fingerprint = fingerprint,
                observation = observation,
            )
        }

        val acceptResult =
            candidate.accept(observation)

        return ProcessingResult(
            candidate = candidate,
            status = acceptResult.toSubmitStatus(),
            shouldRecord = candidate.tryStartRecording(),
        )
    }

    private fun processRetry(candidate: EventCandidate): ProcessingResult =
        ProcessingResult(
            candidate = candidate,
            status =
                if (candidate.status == EventCandidateStatus.RECORDED) {
                    SubmitEventReportStatus.ALREADY_RECORDED
                } else {
                    SubmitEventReportStatus.DUPLICATE
                },
            shouldRecord = candidate.tryStartRecording(),
        )

    private fun createCandidate(
        command: SubmitEventReportCommand,
        fingerprint: EventFingerprint,
        observation: EventObservation,
    ): ProcessingResult {
        val requiredConfirmations =
            confirmationPolicy.requiredConfirmations(
                activeCollectorProvider.activeCollectorCount(),
            )

        val candidate =
            EventCandidate.create(
                fingerprint = fingerprint,
                report = command.report,
                observation = observation,
                requiredConfirmations = requiredConfirmations,
                expiresAt =
                    command.receivedAt.plus(
                        properties.candidateTtl,
                    ),
            )

        candidateRepository.add(candidate)

        return ProcessingResult(
            candidate = candidate,
            status = SubmitEventReportStatus.ACCEPTED,
            shouldRecord = candidate.tryStartRecording(),
        )
    }

    private fun record(
        fingerprint: EventFingerprint,
        processingResult: ProcessingResult,
    ): SubmitEventReportResult {
        val candidate = processingResult.candidate

        try {
            confirmedEventRecorder.record(
                report = candidate.report,
                occurredAt = candidate.referenceObservedAt,
            )
        } catch (exception: Exception) {
            candidateLock.withLock(fingerprint) {
                candidate.recordingFailed()
            }

            throw exception
        }

        candidateLock.withLock(fingerprint) {
            candidate.recordingSucceeded()
        }

        return SubmitEventReportResult(
            candidateId = candidate.id,
            status = SubmitEventReportStatus.RECORDED,
            confirmations = candidate.confirmationCount,
            requiredConfirmations = candidate.requiredConfirmations,
        )
    }

    private fun EventCandidateAcceptResult.toSubmitStatus(): SubmitEventReportStatus =
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

    private data class ProcessingResult(
        val candidate: EventCandidate,
        val status: SubmitEventReportStatus,
        val shouldRecord: Boolean,
    ) {
        fun toResult(): SubmitEventReportResult =
            SubmitEventReportResult(
                candidateId = candidate.id,
                status = status,
                confirmations = candidate.confirmationCount,
                requiredConfirmations = candidate.requiredConfirmations,
            )
    }
}
