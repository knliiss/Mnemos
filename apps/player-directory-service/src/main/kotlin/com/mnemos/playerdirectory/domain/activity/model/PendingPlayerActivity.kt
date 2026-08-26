package com.mnemos.playerdirectory.domain.activity.model

import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import java.time.Duration
import java.time.Instant

data class PendingPlayerActivity(
    val nickname: PlayerNickname,
    val pendingDropCount: Long,
    val lastDropAt: Instant,
    val nextAttemptAt: Instant,
    val leaseUntil: Instant?,
    val consecutiveFailures: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(pendingDropCount > 0) {
            "Pending drop count must be positive"
        }
        require(consecutiveFailures >= 0) {
            "Pending player resolution failure count cannot be negative"
        }
    }

    fun claim(
        now: Instant,
        leaseDuration: Duration,
    ): PendingPlayerActivity =
        copy(
            leaseUntil = now.plus(leaseDuration),
            updatedAt = now,
        )

    fun completeFailure(
        expectedLeaseUntil: Instant,
        completedAt: Instant,
        nextAttemptAt: Instant,
    ): PendingPlayerActivity? {
        if (leaseUntil != expectedLeaseUntil) {
            return null
        }

        return copy(
            nextAttemptAt = nextAttemptAt,
            leaseUntil = null,
            consecutiveFailures = consecutiveFailures + 1,
            updatedAt = completedAt,
        )
    }

    fun completePartialSuccess(
        expectedLeaseUntil: Instant,
        resolvedDropCount: Long,
        completedAt: Instant,
    ): PendingPlayerActivity? {
        if (leaseUntil != expectedLeaseUntil) {
            return null
        }

        val remaining = pendingDropCount - resolvedDropCount

        require(remaining > 0) {
            "Partial success requires pending activity to remain"
        }

        return copy(
            pendingDropCount = remaining,
            nextAttemptAt = completedAt,
            leaseUntil = null,
            consecutiveFailures = 0,
            updatedAt = completedAt,
        )
    }
}
