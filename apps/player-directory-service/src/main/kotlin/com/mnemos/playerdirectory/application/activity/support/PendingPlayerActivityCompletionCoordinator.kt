package com.mnemos.playerdirectory.application.activity.support

import com.mnemos.playerdirectory.application.activity.model.PendingPlayerActivityLease
import com.mnemos.playerdirectory.application.activity.policy.PendingPlayerResolutionPolicy
import com.mnemos.playerdirectory.application.activity.port.out.PendingPlayerActivityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class PendingPlayerActivityCompletionCoordinator(
    private val repository: PendingPlayerActivityRepository,
    private val policy: PendingPlayerResolutionPolicy,
    private val clock: Clock,
) {
    @Transactional
    fun completeSuccess(lease: PendingPlayerActivityLease) {
        val current = repository.findByNicknameForUpdate(lease.nickname) ?: return

        if (current.leaseUntil != lease.leaseUntil) {
            return
        }

        val completedAt = Instant.now(clock)
        val remainingDropCount = current.pendingDropCount - lease.pendingDropCount

        if (remainingDropCount <= 0) {
            repository.delete(current)
            return
        }

        repository.save(
            current.completePartialSuccess(
                expectedLeaseUntil = lease.leaseUntil,
                resolvedDropCount = lease.pendingDropCount,
                completedAt = completedAt,
            ) ?: return,
        )
    }

    @Transactional
    fun completeFailure(lease: PendingPlayerActivityLease) {
        val current = repository.findByNicknameForUpdate(lease.nickname) ?: return

        if (current.leaseUntil != lease.leaseUntil) {
            return
        }

        val completedAt = Instant.now(clock)
        val nextAttemptAt =
            policy.nextAfterFailure(
                currentFailureCount = current.consecutiveFailures,
                completedAt = completedAt,
            )

        repository.save(
            current.completeFailure(
                expectedLeaseUntil = lease.leaseUntil,
                completedAt = completedAt,
                nextAttemptAt = nextAttemptAt,
            ) ?: return,
        )
    }
}
