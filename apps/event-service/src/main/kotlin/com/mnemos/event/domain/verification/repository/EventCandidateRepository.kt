package com.mnemos.event.domain.verification.repository

import com.mnemos.event.domain.verification.model.EventCandidate
import com.mnemos.event.domain.verification.model.EventFingerprint
import java.time.Instant

interface EventCandidateRepository {
    fun findActiveByFingerprint(
        fingerprint: EventFingerprint,
        now: Instant,
    ): List<EventCandidate>

    fun add(candidate: EventCandidate)
}
