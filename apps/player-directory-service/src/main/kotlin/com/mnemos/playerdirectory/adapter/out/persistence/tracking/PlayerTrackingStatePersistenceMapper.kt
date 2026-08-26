package com.mnemos.playerdirectory.adapter.out.persistence.tracking

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState

object PlayerTrackingStatePersistenceMapper {
    fun PlayerTrackingState.toEntity(): PlayerTrackingStateEntity =
        PlayerTrackingStateEntity(
            playerId = playerId.id,
            activeUntil = activeUntil,
            nextPollAt = nextPollAt,
            leaseUntil = leaseUntil,
            lastPollAt = lastPollAt,
            consecutiveFailures = consecutiveFailures,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun PlayerTrackingStateEntity.toDomain(): PlayerTrackingState =
        PlayerTrackingState(
            playerId = CristalixPlayerId(playerId),
            activeUntil = activeUntil,
            nextPollAt = nextPollAt,
            leaseUntil = leaseUntil,
            lastPollAt = lastPollAt,
            consecutiveFailures = consecutiveFailures,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
