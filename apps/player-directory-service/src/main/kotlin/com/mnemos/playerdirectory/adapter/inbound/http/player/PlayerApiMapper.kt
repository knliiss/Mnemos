package com.mnemos.playerdirectory.adapter.inbound.http.player

import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus

object PlayerApiMapper {
    fun Player.toResponse(): PlayerResponse {
        val hidden = presence.status == PlayerPresenceStatus.HIDDEN || presence.privacyHidden
        val publicStatus = if (hidden) PlayerPresenceStatus.HIDDEN else presence.status

        return PlayerResponse(
            playerId = id.id,
            nickname = nickname.value,
            presenceStatus = publicStatus.name,
            realm = presence.realm.takeUnless { hidden },
            realmType = presence.realmType.takeUnless { hidden },
            privacyHidden = hidden,
            lastSeenOnline = presence.lastSeenOnline.takeUnless { hidden },
            observedAt = presence.observedAt,
        )
    }
}
