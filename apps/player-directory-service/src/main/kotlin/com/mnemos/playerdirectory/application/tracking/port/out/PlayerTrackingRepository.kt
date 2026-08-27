package com.mnemos.playerdirectory.application.tracking.port.out

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import java.time.Instant

interface PlayerTrackingRepository {
    fun ensureState(state: PlayerTrackingState)

    fun save(state: PlayerTrackingState): PlayerTrackingState

    fun findByIdForUpdate(playerId: CristalixPlayerId): PlayerTrackingState?

    fun findDueForUpdate(
        now: Instant,
        limit: Int,
    ): List<PlayerTrackingState>
}
