package com.mnemos.profile.adapter.out.persistence.playerstate

import com.mnemos.profile.domain.profile.model.ProfilePlayerState

object ProfilePlayerStatePersistenceMapper {
    fun ProfilePlayerState.toEntity(): ProfilePlayerStateEntity =
        ProfilePlayerStateEntity(
            playerId = playerId,
            nickname = nickname,
            externalVersion = externalVersion,
            presenceStatus = presenceStatus.name,
            realm = realm,
            realmType = realmType,
            privacyHidden = privacyHidden,
            lastSeenOnline = lastSeenOnline,
            presenceObservedAt = presenceObservedAt,
            updatedAt = updatedAt,
        )

    fun ProfilePlayerStateEntity.toDomain(): ProfilePlayerState =
        ProfilePlayerState(
            playerId = playerId,
            nickname = nickname,
            externalVersion = externalVersion,
            presenceStatus = ProfilePlayerState.PresenceStatus.valueOf(presenceStatus),
            realm = realm,
            realmType = realmType,
            privacyHidden = privacyHidden,
            lastSeenOnline = lastSeenOnline,
            presenceObservedAt = presenceObservedAt,
            updatedAt = updatedAt,
        )
}
