package com.mnemos.playerdirectory.application.tracking.usecase.activity

import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingActivityPolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RecordPlayerDropActivityService(
    private val repository: PlayerTrackingRepository,
    private val activityPolicy: PlayerTrackingActivityPolicy,
) : RecordPlayerDropActivityUseCase {
    @Transactional
    override fun execute(
        playerId: CristalixPlayerId,
        occurredAt: Instant,
    ) {
        repository.ensureState(
            PlayerTrackingState.create(
                playerId = playerId,
                nextPollAt = occurredAt,
                createdAt = occurredAt,
            ),
        )

        val current =
            requireNotNull(repository.findByIdForUpdate(playerId)) {
                "Player tracking state must exist after ensureState"
            }
        val nextDropCount = current.observedDropCount + 1
        val nextActiveUntil = occurredAt.plus(activityPolicy.retentionFor(nextDropCount))

        repository.save(
            current.recordDrop(
                occurredAt = occurredAt,
                nextActiveUntil = nextActiveUntil,
            ),
        )
    }
}
