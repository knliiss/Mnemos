package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player

interface PlayerRepository {
    fun ensureState(player: Player): Boolean

    fun save(player: Player): Player

    fun findById(playerId: CristalixPlayerId): Player?

    fun findByIdForUpdate(playerId: CristalixPlayerId): Player?
}
