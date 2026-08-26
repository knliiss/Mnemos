package com.mnemos.playerdirectory.application.tracking.support

import com.mnemos.playerdirectory.application.tracking.model.PlayerTrackingLease
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingSchedulePolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class PlayerTrackingCompletionCoordinator(
    private val repository: PlayerTrackingRepository,
    private val schedulePolicy: PlayerTrackingSchedulePolicy,
    private val clock: Clock,
) {
    @Transactional
    fun completeSuccess(
        lease: PlayerTrackingLease,
        presenceStatus: PlayerPresenceStatus,
    ) {
        val state = repository.findByIdForUpdate(lease.playerId) ?: return
        val completedAt = Instant.now(clock)
        val nextPollAt = schedulePolicy.nextAfterObservation(presenceStatus, completedAt)
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
}
