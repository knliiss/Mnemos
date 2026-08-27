package com.mnemos.profile.adapter.inbound.messaging.player

import com.mnemos.contracts.playerdirectory.v1.PlayerObservedV1
import com.mnemos.contracts.playerdirectory.v1.PlayerPresenceStatusV1
import com.mnemos.profile.domain.profile.model.ProfilePlayerState

object PlayerDirectoryProfileEventMapper {
    fun PlayerObservedV1.toDomain(): ProfilePlayerState =
        ProfilePlayerState(
            playerId = playerId,
            nickname = nickname,
            externalVersion = externalVersion,
            presenceStatus = status.toDomain(),
            realm = realm,
            realmType = realmType,
            privacyHidden = privacyHidden,
            lastSeenOnline = lastSeenOnline,
            presenceObservedAt = observedAt,
            updatedAt = observedAt,
        )

    fun PlayerPresenceStatusV1.toDomain(): ProfilePlayerState.PresenceStatus = ProfilePlayerState.PresenceStatus.valueOf(name)
}
