package com.mnemos.playerdirectory.application.tracking.usecase.claim

import com.mnemos.playerdirectory.application.tracking.model.PlayerTrackingLease
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingExecutionPolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class ClaimDuePlayerTrackingService(
    private val repository: PlayerTrackingRepository,
    private val executionPolicy: PlayerTrackingExecutionPolicy,
    private val clock: Clock,
) : ClaimDuePlayerTrackingUseCase {
    @Transactional
    override fun execute(): List<PlayerTrackingLease> {
        val now = Instant.now(clock)

        return repository
            .findDueForUpdate(
                now = now,
                limit = executionPolicy.batchSize,
            ).map { state ->
                val claimed =
                    state.claim(
                        now = now,
                        leaseDuration = executionPolicy.leaseDuration,
                    )
                val saved = repository.save(claimed)
                val leaseUntil = requireNotNull(saved.leaseUntil) { "Claimed tracking state must have a lease" }

                PlayerTrackingLease(
                    playerId = saved.playerId,
                    leaseUntil = leaseUntil,
                )
            }
    }
}
