package com.mnemos.playerdirectory.domain.player.model

import java.time.Instant

data class PlayerObservation(
    val playerId: CristalixPlayerId,
    val nickname: PlayerNickname,
    val externalVersion: Long,
    val externalUpdatedAt: Instant?,
    val presence: PlayerPresence,
) {
    init {
        require(externalVersion >= 0) { "External player version cannot be negative" }
    }
}
