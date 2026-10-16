package com.mnemos.playerdirectory.application.accountlink.port.out

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId

interface PlayerAccountLinkRepository {
    fun save(link: PlayerAccountLink)

    fun existsByPlayerId(playerId: CristalixPlayerId): Boolean
}
