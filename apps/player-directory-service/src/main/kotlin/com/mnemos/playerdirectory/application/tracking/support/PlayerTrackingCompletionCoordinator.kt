package com.mnemos.playerdirectory.application.tracking.support

import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.application.tracking.model.PlayerTrackingLease
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingActivityPolicy
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingSchedulePolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class PlayerTrackingCompletionCoordinator(
    private val repository: PlayerTrackingRepository,
    private val subscriptionRepository: PresenceSubscriptionRepository,
    private val linkedAccountRepository: PlayerAccountLinkRepository,
    private val activityPolicy: PlayerTrackingActivityPolicy,
    private val schedulePolicy: PlayerTrackingSchedulePolicy,
    private val clock: Clock,
) {
    @Transactional
    fun completeSuccess(
        lease: PlayerTrackingLease,
        observation: PlayerObservation,
    ) {
        val current = repository.findByIdForUpdate(lease.playerId) ?: return
        val completedAt = Instant.now(clock)
        val saoOnline = observation.presence.isSaoOnline()
        val state =
            if (saoOnline) {
                val activeUntil =
                    observation.presence.observedAt.plus(
                        activityPolicy.retentionFor(current.observedDropCount),
                    )

                current.recordSaoOnline(
                    observedAt = observation.presence.observedAt,
                    nextActiveUntil = activeUntil,
                )
            } else {
                current
            }
        val hasSubscription = subscriptionRepository.existsByPlayerId(lease.playerId)
        val idleLinkedAccount =
            !hasSubscription &&
                (state.activeUntil == null || !state.activeUntil.isAfter(completedAt)) &&
                linkedAccountRepository.existsByPlayerId(lease.playerId)
        val nextPollAt =
            schedulePolicy.nextAfterObservation(
                status = observation.presence.status,
                saoOnline = saoOnline,
                hasSubscription = hasSubscription,
                highActivity = activityPolicy.isHighActivity(state.observedDropCount),
                completedAt = completedAt,
                idleLinkedAccount = idleLinkedAccount,
            )
        val completed =
            state.completeSuccess(
                expectedLeaseUntil = lease.leaseUntil,
                completedAt = completedAt,
                nextPollAt = nextPollAt,
            ) ?: return

        repository.save(completed)
    }

    @Transactional
    fun completeFailure(lease: PlayerTrackingLease) {
        val state = repository.findByIdForUpdate(lease.playerId) ?: return
        val completedAt = Instant.now(clock)
        val nextPollAt =
            schedulePolicy.nextAfterFailure(
                currentFailureCount = state.consecutiveFailures,
                completedAt = completedAt,
            )
        val completed =
            state.completeFailure(
                expectedLeaseUntil = lease.leaseUntil,
                completedAt = completedAt,
                nextPollAt = nextPollAt,
            ) ?: return

        repository.save(completed)
    }

    private fun com.mnemos.playerdirectory.domain.player.model.PlayerPresence.isSaoOnline(): Boolean {
        if (status != PlayerPresenceStatus.ONLINE) {
            return false
        }

        return realmType.equals("SAO", ignoreCase = true) ||
            SAO_REALM_PATTERN.matches(realm.orEmpty())
    }

    companion object {
        private val SAO_REALM_PATTERN = Regex("^SAO-[1-9][0-9]*$")
    }
}
