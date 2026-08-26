package com.mnemos.playerdirectory.adapter.out.persistence.player

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus

object PlayerPersistenceMapper {
    fun Player.toEntity(): PlayerEntity =
        PlayerEntity(
            playerId = id.id,
            currentNickname = nickname.value,
            normalizedNickname = nickname.normalized(),
            externalVersion = externalVersion,
            externalUpdatedAt = externalUpdatedAt,
            presenceStatus = presence.status.name,
            realm = presence.realm,
            realmType = presence.realmType,
            privacyHidden = presence.privacyHidden,
            lastSeenOnline = presence.lastSeenOnline,
            presenceObservedAt = presence.observedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun PlayerEntity.toDomain(): Player =
        Player(
            id = CristalixPlayerId(playerId),
            nickname = PlayerNickname.of(currentNickname),
            externalVersion = externalVersion,
            externalUpdatedAt = externalUpdatedAt,
            presence =
                PlayerPresence(
                    status = PlayerPresenceStatus.valueOf(presenceStatus),
                    realm = realm,
                    realmType = realmType,
                    privacyHidden = privacyHidden,
                    lastSeenOnline = lastSeenOnline,
                    observedAt = presenceObservedAt,
                ),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
