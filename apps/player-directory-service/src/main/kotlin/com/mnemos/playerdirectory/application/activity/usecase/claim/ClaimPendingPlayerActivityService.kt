package com.mnemos.playerdirectory.application.activity.usecase.claim

import com.mnemos.playerdirectory.application.activity.model.PendingPlayerActivityLease
import com.mnemos.playerdirectory.application.activity.policy.PendingPlayerResolutionPolicy
import com.mnemos.playerdirectory.application.activity.port.out.PendingPlayerActivityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class ClaimPendingPlayerActivityService(
    private val repository: PendingPlayerActivityRepository,
    private val policy: PendingPlayerResolutionPolicy,
    private val clock: Clock,
) : ClaimPendingPlayerActivityUseCase {
    @Transactional
    override fun execute(): List<PendingPlayerActivityLease> {
        val now = Instant.now(clock)
        val due = repository.findDueForUpdate(now, policy.batchSize)

        return due.map { activity ->
            val claimed = activity.claim(now, policy.leaseDuration)
            val saved = repository.save(claimed)

            PendingPlayerActivityLease(
                nickname = saved.nickname,
                pendingDropCount = saved.pendingDropCount,
                lastDropAt = saved.lastDropAt,
                leaseUntil = requireNotNull(saved.leaseUntil),
            )
        }
    }
}
