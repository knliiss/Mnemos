package com.mnemos.profile.adapter.out.persistence.playerstate

import com.mnemos.profile.adapter.out.persistence.playerstate.ProfilePlayerStatePersistenceMapper.toDomain
import com.mnemos.profile.adapter.out.persistence.playerstate.ProfilePlayerStatePersistenceMapper.toEntity
import com.mnemos.profile.application.profile.port.out.ProfilePlayerStateRepository
import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProfilePlayerStateRepositoryAdapter(
    private val repository: ProfilePlayerStateJpaRepository,
) : ProfilePlayerStateRepository {
    override fun ensureState(state: ProfilePlayerState) {
        repository.insertIfAbsent(
            playerId = state.playerId,
            nickname = state.nickname,
            externalVersion = state.externalVersion,
            presenceStatus = state.presenceStatus.name,
            realm = state.realm,
            realmType = state.realmType,
            privacyHidden = state.privacyHidden,
            lastSeenOnline = state.lastSeenOnline,
            presenceObservedAt = state.presenceObservedAt,
            updatedAt = state.updatedAt,
        )
    }

    override fun save(state: ProfilePlayerState): ProfilePlayerState = repository.save(state.toEntity()).toDomain()

    override fun findByPlayerId(playerId: UUID): ProfilePlayerState? = repository.findById(playerId).orElse(null)?.toDomain()

    override fun findByPlayerIdForUpdate(playerId: UUID): ProfilePlayerState? = repository.findByPlayerIdForUpdate(playerId)?.toDomain()

    override fun findByPlayerIds(playerIds: Collection<UUID>): Map<UUID, ProfilePlayerState> =
        if (playerIds.isEmpty()) {
            emptyMap()
        } else {
            repository
                .findAllByPlayerIdIn(playerIds)
                .associate { entity -> entity.playerId to entity.toDomain() }
        }
}
