package com.mnemos.playerdirectory.adapter.inbound.http.player

import com.mnemos.playerdirectory.domain.player.model.Player

object PlayerApiMapper {
    fun Player.toResponse(): PlayerResponse =
        PlayerResponse(
            playerId = id.id,
            nickname = nickname.value,
            presenceStatus = presence.status.name,
            realm = presence.realm,
            realmType = presence.realmType,
            privacyHidden = presence.privacyHidden,
            lastSeenOnline = presence.lastSeenOnline,
            observedAt = presence.observedAt,
        )
}
