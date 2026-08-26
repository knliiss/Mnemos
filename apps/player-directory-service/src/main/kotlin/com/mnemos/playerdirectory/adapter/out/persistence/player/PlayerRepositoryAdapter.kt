package com.mnemos.playerdirectory.adapter.out.persistence.player

import com.mnemos.playerdirectory.adapter.out.persistence.player.PlayerPersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.player.PlayerPersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Repository

@Repository
class PlayerRepositoryAdapter(
    private val repository: PlayerJpaRepository,
) : PlayerRepository {
    override fun save(player: Player): Player = repository.save(player.toEntity()).toDomain()

    override fun findById(playerId: CristalixPlayerId): Player? = repository.findById(playerId.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(playerId: CristalixPlayerId): Player? = repository.findByPlayerIdForUpdate(playerId.id)?.toDomain()

    override fun findByCurrentNickname(nickname: PlayerNickname): Player? =
        repository.findByNormalizedNickname(nickname.normalized())?.toDomain()
}
