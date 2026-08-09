package com.mnemos.event.application.collector.port.out

import com.mnemos.event.domain.verification.model.EventFingerprint

interface EventCandidateLock {
    fun <T> withLock(
        fingerprint: EventFingerprint,
        action: () -> T,
    ): T
}
