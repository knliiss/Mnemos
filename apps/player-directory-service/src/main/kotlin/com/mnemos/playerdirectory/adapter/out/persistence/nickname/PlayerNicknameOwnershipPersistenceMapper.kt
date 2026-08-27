package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameOwnership

object PlayerNicknameOwnershipPersistenceMapper {
    fun PlayerNicknameOwnership.toEntity(): PlayerNicknameOwnershipEntity =
        PlayerNicknameOwnershipEntity(
            normalizedNickname = nickname.normalized(),
            nickname = nickname.value,
            playerId = playerId.id,
            externalVersion = externalVersion,
            observedAt = observedAt,
        )

    fun PlayerNicknameOwnershipEntity.toDomain(): PlayerNicknameOwnership =
        PlayerNicknameOwnership(
            playerId = CristalixPlayerId(playerId),
            nickname = PlayerNickname.of(nickname),
            externalVersion = externalVersion,
            observedAt = observedAt,
        )
}
