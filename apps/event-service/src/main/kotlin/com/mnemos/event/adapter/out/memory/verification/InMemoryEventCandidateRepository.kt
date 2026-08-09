package com.mnemos.event.adapter.out.memory.verification

import com.mnemos.event.domain.verification.model.EventCandidate
import com.mnemos.event.domain.verification.model.EventFingerprint
import com.mnemos.event.domain.verification.repository.EventCandidateRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryEventCandidateRepository : EventCandidateRepository {
    private val candidates =
        ConcurrentHashMap<EventFingerprint, MutableList<EventCandidate>>()

    override fun findActiveByFingerprint(
        fingerprint: EventFingerprint,
        now: Instant,
    ): List<EventCandidate> {
        val bucket =
            candidates[fingerprint]
                ?: return emptyList()

        synchronized(bucket) {
            bucket.removeIf { it.isExpired(now) }

            if (bucket.isEmpty()) {
                candidates.remove(fingerprint, bucket)
                return emptyList()
            }

            return bucket.toList()
        }
    }

    override fun add(candidate: EventCandidate) {
        val bucket =
            candidates.computeIfAbsent(candidate.fingerprint) {
                mutableListOf()
            }

        synchronized(bucket) {
            bucket += candidate
        }
    }
}
