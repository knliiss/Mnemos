package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryEntry

interface PlayerNicknameHistoryRepository {
    fun save(entry: PlayerNicknameHistoryEntry): PlayerNicknameHistoryEntry

    fun findOpenByPlayerIdForUpdate(playerId: CristalixPlayerId): PlayerNicknameHistoryEntry?
}
