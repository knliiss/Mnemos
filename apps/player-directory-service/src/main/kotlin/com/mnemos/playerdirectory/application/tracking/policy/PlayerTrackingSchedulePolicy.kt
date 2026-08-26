package com.mnemos.playerdirectory.application.tracking.policy

import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import java.time.Duration
import java.time.Instant
import kotlin.math.min

class PlayerTrackingSchedulePolicy(
    private val subscriptionPollInterval: Duration,
    private val unverifiablePollInterval: Duration,
    private val failureBackoff: Duration,
    private val maximumFailureBackoff: Duration,
) {
    fun nextAfterVerifiedSubscription(observedAt: Instant): Instant = observedAt.plus(subscriptionPollInterval)

    fun nextAfterObservation(
        status: PlayerPresenceStatus,
        completedAt: Instant,
    ): Instant =
        when (status) {
            PlayerPresenceStatus.ONLINE,
            PlayerPresenceStatus.OFFLINE,
            -> completedAt.plus(subscriptionPollInterval)

            PlayerPresenceStatus.UNKNOWN,
            PlayerPresenceStatus.HIDDEN,
            -> completedAt.plus(unverifiablePollInterval)
        }

    fun nextAfterFailure(
        currentFailureCount: Int,
        completedAt: Instant,
    ): Instant {
        val exponent = min(currentFailureCount, MAX_BACKOFF_EXPONENT)
        val multiplier = 1L shl exponent
        val calculated = failureBackoff.multipliedBy(multiplier)
        val delay = minOf(calculated, maximumFailureBackoff)

        return completedAt.plus(delay)
    }

    companion object {
        private const val MAX_BACKOFF_EXPONENT = 20
    }
}
