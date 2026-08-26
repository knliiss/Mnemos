package com.mnemos.playerdirectory.adapter.inbound.http.internal

import com.mnemos.contracts.playerdirectory.internal.v1.InternalPlayerResponse
import com.mnemos.contracts.playerdirectory.v1.PlayerPresenceStatusV1
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus

object InternalPlayerApiMapper {
    fun Player.toResponse(): InternalPlayerResponse =
        InternalPlayerResponse(
            playerId = id.id,
            nickname = nickname.value,
            externalVersion = externalVersion,
            presenceStatus = presence.status.toContract(),
            realm = presence.realm,
            realmType = presence.realmType,
            privacyHidden = presence.privacyHidden,
            lastSeenOnline = presence.lastSeenOnline,
            observedAt = presence.observedAt,
        )

    private fun PlayerPresenceStatus.toContract(): PlayerPresenceStatusV1 = PlayerPresenceStatusV1.valueOf(name)
}
