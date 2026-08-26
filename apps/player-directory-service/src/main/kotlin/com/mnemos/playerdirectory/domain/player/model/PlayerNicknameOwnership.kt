package com.mnemos.playerdirectory.domain.player.model

import java.time.Instant

data class PlayerNicknameOwnership(
    val playerId: CristalixPlayerId,
    val nickname: PlayerNickname,
    val externalVersion: Long,
    val observedAt: Instant,
) {
    init {
        require(externalVersion >= 0) { "Nickname ownership external version cannot be negative" }
    }
}
