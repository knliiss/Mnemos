package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player

interface PlayerRepository {
    fun ensureState(player: Player): Boolean

    fun save(player: Player): Player

    fun findById(playerId: CristalixPlayerId): Player?

    fun findByIds(playerIds: Set<CristalixPlayerId>): Map<CristalixPlayerId, Player> =
        playerIds
            .mapNotNull { playerId ->
                findById(playerId)?.let { player -> playerId to player }
            }.toMap()

    fun findByIdForUpdate(playerId: CristalixPlayerId): Player?
}
