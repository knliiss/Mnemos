package com.mnemos.event.application.playeridentity.port.out

import com.mnemos.event.application.playeridentity.model.PlayerIdentityObservation
import java.util.UUID

interface PlayerIdentityProjectionRepository {
    fun apply(observation: PlayerIdentityObservation)

    fun findCurrentOwnerId(nickname: String): UUID?

    fun findCurrentNickname(playerId: UUID): String?
}
