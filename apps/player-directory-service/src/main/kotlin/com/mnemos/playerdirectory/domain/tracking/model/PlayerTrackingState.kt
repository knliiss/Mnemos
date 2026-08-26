package com.mnemos.playerdirectory.domain.tracking.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Duration
import java.time.Instant

data class PlayerTrackingState(
    val playerId: CristalixPlayerId,
    val activeUntil: Instant?,
    val nextPollAt: Instant,
    val leaseUntil: Instant?,
    val lastPollAt: Instant?,
    val consecutiveFailures: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(consecutiveFailures >= 0) { "Tracking failure count cannot be negative" }
    }

    fun claim(
        now: Instant,
        leaseDuration: Duration,
    ): PlayerTrackingState =
        copy(
            leaseUntil = now.plus(leaseDuration),
            updatedAt = now,
        )

    fun completeSuccess(
        expectedLeaseUntil: Instant,
        completedAt: Instant,
        nextPollAt: Instant,
    ): PlayerTrackingState? {
        if (leaseUntil != expectedLeaseUntil) {
            return null
        }

        return copy(
            nextPollAt = nextPollAt,
            leaseUntil = null,
            lastPollAt = completedAt,
            consecutiveFailures = 0,
            updatedAt = completedAt,
        )
    }

    fun completeFailure(
        expectedLeaseUntil: Instant,
        completedAt: Instant,
        nextPollAt: Instant,
    ): PlayerTrackingState? {
        if (leaseUntil != expectedLeaseUntil) {
            return null
        }

        return copy(
            nextPollAt = nextPollAt,
            leaseUntil = null,
            lastPollAt = completedAt,
            consecutiveFailures = consecutiveFailures + 1,
            updatedAt = completedAt,
        )
    }

    companion object {
        fun create(
            playerId: CristalixPlayerId,
            nextPollAt: Instant,
            createdAt: Instant,
            activeUntil: Instant? = null,
        ): PlayerTrackingState =
            PlayerTrackingState(
                playerId = playerId,
                activeUntil = activeUntil,
                nextPollAt = nextPollAt,
                leaseUntil = null,
                lastPollAt = null,
                consecutiveFailures = 0,
                createdAt = createdAt,
                updatedAt = createdAt,
            )
    }
}
