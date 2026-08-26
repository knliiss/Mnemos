package com.mnemos.playerdirectory.application.tracking.usecase.activity

import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingActivityPolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordPlayerDropActivityService(
    private val repository: PlayerTrackingRepository,
    private val activityPolicy: PlayerTrackingActivityPolicy,
) : RecordPlayerDropActivityUseCase {
    @Transactional
    override fun execute(command: RecordPlayerDropActivityCommand) {
        repository.ensureState(
            PlayerTrackingState.create(
                playerId = command.playerId,
                nextPollAt = command.occurredAt,
                createdAt = command.occurredAt,
            ),
        )

        val current =
            requireNotNull(repository.findByIdForUpdate(command.playerId)) {
                "Player tracking state must exist after ensureState"
            }
        val nextDropCount = current.observedDropCount + command.dropCount
        val nextActiveUntil = command.occurredAt.plus(activityPolicy.retentionFor(nextDropCount))

        repository.save(
            current.recordDrops(
                dropCount = command.dropCount,
                occurredAt = command.occurredAt,
                nextActiveUntil = nextActiveUntil,
            ),
        )
    }
}
