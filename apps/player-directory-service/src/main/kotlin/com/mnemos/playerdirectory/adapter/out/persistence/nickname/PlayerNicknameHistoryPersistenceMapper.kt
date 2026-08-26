package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryEntry
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryId

object PlayerNicknameHistoryPersistenceMapper {
    fun PlayerNicknameHistoryEntry.toEntity(): PlayerNicknameHistoryEntity =
        PlayerNicknameHistoryEntity(
            id = id.id,
            playerId = playerId.id,
            nickname = nickname.value,
            normalizedNickname = nickname.normalized(),
            externalVersion = externalVersion,
            validFrom = validFrom,
            validUntil = validUntil,
        )

    fun PlayerNicknameHistoryEntity.toDomain(): PlayerNicknameHistoryEntry =
        PlayerNicknameHistoryEntry(
            id = PlayerNicknameHistoryId(id),
            playerId = CristalixPlayerId(playerId),
            nickname = PlayerNickname.of(nickname),
            externalVersion = externalVersion,
            validFrom = validFrom,
            validUntil = validUntil,
        )
}
