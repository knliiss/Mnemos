package com.mnemos.event.application.playeridentity.port.out

import com.mnemos.event.application.playeridentity.model.PlayerIdentityObservation
import com.mnemos.event.domain.player.model.CristalixPlayerId

interface PlayerIdentityProjectionRepository {
    fun apply(observation: PlayerIdentityObservation): Boolean

    fun findCurrentOwnerId(nickname: String): CristalixPlayerId?

    fun findCurrentNickname(playerId: CristalixPlayerId): String?
}
