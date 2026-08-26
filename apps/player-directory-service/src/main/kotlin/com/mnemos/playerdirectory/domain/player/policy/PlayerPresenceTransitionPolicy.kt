package com.mnemos.playerdirectory.domain.player.policy

import java.time.Duration
import java.time.Instant

class PlayerPresenceTransitionPolicy(
    private val offlineConfirmationDelay: Duration,
    private val requiredOfflineObservations: Int,
) {
    init {
        require(!offlineConfirmationDelay.isNegative) {
            "Offline confirmation delay cannot be negative"
        }
        require(requiredOfflineObservations >= 2) {
            "At least two offline observations are required"
        }
    }

    fun isOfflineConfirmed(
        candidateSince: Instant,
        candidateCount: Int,
        observedAt: Instant,
    ): Boolean {
        if (candidateCount < requiredOfflineObservations) {
            return false
        }

        return !observedAt.isBefore(candidateSince.plus(offlineConfirmationDelay))
    }
}
