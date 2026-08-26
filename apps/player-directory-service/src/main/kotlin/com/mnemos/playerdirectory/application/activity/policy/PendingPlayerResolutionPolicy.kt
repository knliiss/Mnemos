package com.mnemos.playerdirectory.application.activity.policy

import java.time.Duration
import java.time.Instant
import kotlin.math.min

class PendingPlayerResolutionPolicy(
    val batchSize: Int,
    val leaseDuration: Duration,
    private val retryBackoff: Duration,
    private val maximumRetryBackoff: Duration,
) {
    init {
        require(batchSize in 1..200) {
            "Pending player resolution batch size must be between 1 and 200"
        }
        require(!leaseDuration.isNegative && !leaseDuration.isZero) {
            "Pending player resolution lease duration must be positive"
        }
        require(!retryBackoff.isNegative && !retryBackoff.isZero) {
            "Pending player resolution retry backoff must be positive"
        }
        require(maximumRetryBackoff >= retryBackoff) {
            "Maximum pending player resolution retry backoff cannot be smaller than the base backoff"
        }
    }

    fun nextAfterFailure(
        currentFailureCount: Int,
        completedAt: Instant,
    ): Instant {
        val exponent = min(currentFailureCount, MAX_BACKOFF_EXPONENT)
        val multiplier = 1L shl exponent
        val calculated = retryBackoff.multipliedBy(multiplier)

        return completedAt.plus(minOf(calculated, maximumRetryBackoff))
    }

    companion object {
        private const val MAX_BACKOFF_EXPONENT = 20
    }
}
