package com.mnemos.playerdirectory.application.tracking.policy

import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import java.time.Duration
import java.time.Instant
import kotlin.math.min

class PlayerTrackingSchedulePolicy(
    private val subscriptionPollInterval: Duration,
    private val saoOnlinePollInterval: Duration,
    private val highActivityPollInterval: Duration,
    private val lowActivityPollInterval: Duration,
    private val unverifiablePollInterval: Duration,
    private val failureBackoff: Duration,
    private val maximumFailureBackoff: Duration,
    private val linkedAccountPollInterval: Duration = Duration.ofHours(6),
) {
    fun nextAfterVerifiedSubscription(observedAt: Instant): Instant = observedAt.plus(subscriptionPollInterval)

    fun nextAfterObservation(
        status: PlayerPresenceStatus,
        saoOnline: Boolean,
        hasSubscription: Boolean,
        highActivity: Boolean,
        completedAt: Instant,
        idleLinkedAccount: Boolean = false,
    ): Instant {
        val interval =
            when {
                idleLinkedAccount -> linkedAccountPollInterval
                status == PlayerPresenceStatus.UNKNOWN || status == PlayerPresenceStatus.HIDDEN -> unverifiablePollInterval
                hasSubscription -> subscriptionPollInterval
                saoOnline -> saoOnlinePollInterval
                highActivity -> highActivityPollInterval
                else -> lowActivityPollInterval
            }

        return completedAt.plus(interval)
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
