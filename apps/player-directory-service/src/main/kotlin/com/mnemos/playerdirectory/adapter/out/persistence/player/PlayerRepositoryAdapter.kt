package com.mnemos.playerdirectory.adapter.out.persistence.player

import com.mnemos.playerdirectory.adapter.out.persistence.player.PlayerPersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.player.PlayerPersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import org.springframework.stereotype.Repository

@Repository
class PlayerRepositoryAdapter(
    private val repository: PlayerJpaRepository,
) : PlayerRepository {
    override fun ensureState(player: Player): Boolean =
        repository.insertIfAbsent(
            playerId = player.id.id,
            currentNickname = player.nickname.value,
            normalizedNickname = player.nickname.normalized(),
            externalVersion = player.externalVersion,
            externalUpdatedAt = player.externalUpdatedAt,
            presenceStatus = player.presence.status.name,
            realm = player.presence.realm,
            realmType = player.presence.realmType,
            privacyHidden = player.presence.privacyHidden,
            lastSeenOnline = player.presence.lastSeenOnline,
            presenceObservedAt = player.presence.observedAt,
            lastVerifiedPresenceStatus = player.lastVerifiedPresenceStatus?.name,
            offlineCandidateSince = player.offlineCandidateSince,
            offlineCandidateCount = player.offlineCandidateCount,
            createdAt = player.createdAt,
            updatedAt = player.updatedAt,
        ) > 0

    override fun save(player: Player): Player = repository.save(player.toEntity()).toDomain()

    override fun findById(playerId: CristalixPlayerId): Player? = repository.findById(playerId.id).orElse(null)?.toDomain()

    override fun findByIds(playerIds: Set<CristalixPlayerId>): Map<CristalixPlayerId, Player> =
        repository
            .findAllById(playerIds.map { playerId -> playerId.id })
            .map { entity -> entity.toDomain() }
            .associateBy { player -> player.id }

    override fun findByIdForUpdate(playerId: CristalixPlayerId): Player? = repository.findByPlayerIdForUpdate(playerId.id)?.toDomain()
}
