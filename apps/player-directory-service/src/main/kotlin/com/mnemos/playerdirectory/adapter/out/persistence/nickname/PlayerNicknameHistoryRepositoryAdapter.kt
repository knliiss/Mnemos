package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import com.mnemos.playerdirectory.adapter.out.persistence.nickname.PlayerNicknameHistoryPersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.nickname.PlayerNicknameHistoryPersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameHistoryRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryEntry
import org.springframework.stereotype.Repository

@Repository
class PlayerNicknameHistoryRepositoryAdapter(
    private val repository: PlayerNicknameHistoryJpaRepository,
) : PlayerNicknameHistoryRepository {
    override fun save(entry: PlayerNicknameHistoryEntry): PlayerNicknameHistoryEntry = repository.save(entry.toEntity()).toDomain()

    override fun findOpenByPlayerIdForUpdate(playerId: CristalixPlayerId): PlayerNicknameHistoryEntry? =
        repository.findOpenByPlayerIdForUpdate(playerId.id)?.toDomain()
}
