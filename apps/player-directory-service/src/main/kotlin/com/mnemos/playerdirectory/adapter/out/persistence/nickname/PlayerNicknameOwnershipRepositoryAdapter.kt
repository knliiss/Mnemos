package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import com.mnemos.playerdirectory.adapter.out.persistence.nickname.PlayerNicknameOwnershipPersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.nickname.PlayerNicknameOwnershipPersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameOwnership
import org.springframework.stereotype.Repository

@Repository
class PlayerNicknameOwnershipRepositoryAdapter(
    private val repository: PlayerNicknameOwnershipJpaRepository,
) : PlayerNicknameOwnershipRepository {
    override fun save(ownership: PlayerNicknameOwnership): PlayerNicknameOwnership =
        repository
            .save(ownership.toEntity())
            .toDomain()

    override fun delete(ownership: PlayerNicknameOwnership) {
        repository.deleteById(ownership.nickname.normalized())
        repository.flush()
    }

    override fun findByNickname(nickname: PlayerNickname): PlayerNicknameOwnership? =
        repository
            .findById(nickname.normalized())
            .orElse(null)
            ?.toDomain()

    override fun findByNormalizedNicknamesForUpdate(normalizedNicknames: Set<String>): List<PlayerNicknameOwnership> =
        repository
            .findByNormalizedNicknamesForUpdate(normalizedNicknames)
            .map { ownership -> ownership.toDomain() }
}
