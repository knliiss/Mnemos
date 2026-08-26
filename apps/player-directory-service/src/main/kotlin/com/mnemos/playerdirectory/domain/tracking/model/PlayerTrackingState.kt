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
    val observedDropCount: Long = 0,
    val lastDropAt: Instant? = null,
    val lastSaoOnlineAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(consecutiveFailures >= 0) {
            "Tracking failure count cannot be negative"
        }
        require(observedDropCount >= 0) {
            "Observed drop count cannot be negative"
        }
    }

    fun claim(
        now: Instant,
        leaseDuration: Duration,
    ): PlayerTrackingState =
        copy(
            leaseUntil = now.plus(leaseDuration),
            updatedAt = now,
        )

    fun recordDrops(
        dropCount: Long,
        occurredAt: Instant,
        nextActiveUntil: Instant,
    ): PlayerTrackingState {
        require(dropCount > 0) {
            "Recorded drop count must be positive"
        }

        return copy(
            activeUntil = maxInstant(activeUntil, nextActiveUntil),
            nextPollAt = minOf(nextPollAt, occurredAt),
            observedDropCount = observedDropCount + dropCount,
            lastDropAt = maxInstant(lastDropAt, occurredAt),
            updatedAt = maxOf(updatedAt, occurredAt),
        )
    }

    fun recordSaoOnline(
        observedAt: Instant,
        nextActiveUntil: Instant,
    ): PlayerTrackingState =
        copy(
            activeUntil = maxInstant(activeUntil, nextActiveUntil),
            lastSaoOnlineAt = maxInstant(lastSaoOnlineAt, observedAt),
            updatedAt = maxOf(updatedAt, observedAt),
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

    private fun maxInstant(
        current: Instant?,
        candidate: Instant,
    ): Instant = current?.let { maxOf(it, candidate) } ?: candidate

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
                observedDropCount = 0,
                lastDropAt = null,
                lastSaoOnlineAt = null,
                createdAt = createdAt,
                updatedAt = createdAt,
            )
    }
}
