package com.mnemos.event.adapter.out.memory.verification

import com.mnemos.event.application.collector.port.out.EventCandidateLock
import com.mnemos.event.domain.verification.model.EventFingerprint
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class InMemoryEventCandidateLock : EventCandidateLock {
    private val locks = Array(LOCK_COUNT) { ReentrantLock() }

    override fun <T> withLock(
        fingerprint: EventFingerprint,
        action: () -> T,
    ): T {
        val index = (fingerprint.hashCode() and Int.MAX_VALUE) % locks.size

        return locks[index].withLock(action)
    }

    companion object {
        private const val LOCK_COUNT = 256
    }
}
