package com.mnemos.event.domain.verification.model

import com.mnemos.event.domain.verification.model.report.CollectorEventReport
import java.time.Duration
import java.time.Instant
import java.util.UUID

class EventCandidate private constructor(
    val id: EventCandidateId,
    val fingerprint: EventFingerprint,
    val report: CollectorEventReport,
    val referenceObservedAt: Instant,
    val expiresAt: Instant,
    val requiredConfirmations: Int,
    private val observations: MutableMap<CollectorId, EventObservation>,
    status: EventCandidateStatus,
) {
    var status: EventCandidateStatus = status
        private set

    val confirmationCount: Int
        get() = observations.size

    fun hasCollector(collectorId: CollectorId): Boolean = observations.containsKey(collectorId)

    fun hasMessage(
        collectorId: CollectorId,
        messageId: UUID,
    ): Boolean = observations[collectorId]?.messageId == messageId

    fun matchesTime(
        observedAt: Instant,
        matchingWindow: Duration,
    ): Boolean =
        Duration
            .between(referenceObservedAt, observedAt)
            .abs() <= matchingWindow

    fun isExpired(now: Instant): Boolean =
        when (status) {
            EventCandidateStatus.PENDING,
            EventCandidateStatus.RECORDED,
            ->
                !now.isBefore(expiresAt)

            EventCandidateStatus.CONFIRMED,
            EventCandidateStatus.RECORDING,
            ->
                false
        }

    fun accept(observation: EventObservation): EventCandidateAcceptResult {
        val existing = observations[observation.collectorId]

        if (existing?.messageId == observation.messageId) {
            return EventCandidateAcceptResult.DUPLICATE
        }

        check(existing == null) {
            "Collector already belongs to this candidate"
        }

        observations[observation.collectorId] = observation

        if (
            status == EventCandidateStatus.PENDING &&
            confirmationCount >= requiredConfirmations
        ) {
            status = EventCandidateStatus.CONFIRMED
            return EventCandidateAcceptResult.CONFIRMED
        }

        return when (status) {
            EventCandidateStatus.PENDING ->
                EventCandidateAcceptResult.ACCEPTED

            EventCandidateStatus.CONFIRMED,
            EventCandidateStatus.RECORDING,
            ->
                EventCandidateAcceptResult.ALREADY_CONFIRMED

            EventCandidateStatus.RECORDED ->
                EventCandidateAcceptResult.ALREADY_RECORDED
        }
    }

    fun tryStartRecording(): Boolean {
        if (status != EventCandidateStatus.CONFIRMED) {
            return false
        }

        status = EventCandidateStatus.RECORDING
        return true
    }

    fun recordingSucceeded() {
        check(status == EventCandidateStatus.RECORDING)

        status = EventCandidateStatus.RECORDED
    }

    fun recordingFailed() {
        check(status == EventCandidateStatus.RECORDING)

        status = EventCandidateStatus.CONFIRMED
    }

    companion object {
        fun create(
            fingerprint: EventFingerprint,
            report: CollectorEventReport,
            observation: EventObservation,
            requiredConfirmations: Int,
            expiresAt: Instant,
        ): EventCandidate {
            require(requiredConfirmations > 0)

            val status =
                if (requiredConfirmations == 1) {
                    EventCandidateStatus.CONFIRMED
                } else {
                    EventCandidateStatus.PENDING
                }

            return EventCandidate(
                id = EventCandidateId.generate(),
                fingerprint = fingerprint,
                report = report,
                referenceObservedAt = observation.observedAt,
                expiresAt = expiresAt,
                requiredConfirmations = requiredConfirmations,
                observations =
                    mutableMapOf(
                        observation.collectorId to observation,
                    ),
                status = status,
            )
        }
    }
}
