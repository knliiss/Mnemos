package com.mnemos.playerdirectory.adapter.out.persistence.tracking

import com.mnemos.playerdirectory.adapter.out.persistence.tracking.PlayerTrackingStatePersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.tracking.PlayerTrackingStatePersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PlayerTrackingRepositoryAdapter(
    private val repository: PlayerTrackingStateJpaRepository,
) : PlayerTrackingRepository {
    override fun ensureState(state: PlayerTrackingState) {
        repository.ensureState(
            playerId = state.playerId.id,
            activeUntil = state.activeUntil,
            nextPollAt = state.nextPollAt,
            observedDropCount = state.observedDropCount,
            lastDropAt = state.lastDropAt,
            lastSaoOnlineAt = state.lastSaoOnlineAt,
            createdAt = state.createdAt,
            updatedAt = state.updatedAt,
        )
    }

    override fun save(state: PlayerTrackingState): PlayerTrackingState =
        repository
            .save(state.toEntity())
            .toDomain()

    override fun findByIdForUpdate(playerId: CristalixPlayerId): PlayerTrackingState? =
        repository.findByPlayerIdForUpdate(playerId.id)?.toDomain()

    override fun findDueForUpdate(
        now: Instant,
        limit: Int,
    ): List<PlayerTrackingState> =
        repository
            .findDueForUpdate(
                now = now,
                limit = limit,
            ).map { state -> state.toDomain() }
}
