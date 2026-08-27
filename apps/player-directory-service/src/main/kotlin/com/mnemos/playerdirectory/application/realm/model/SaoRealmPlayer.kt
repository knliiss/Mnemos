package com.mnemos.playerdirectory.application.realm.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Instant
import java.util.UUID

data class SaoRealmPlayer(
    val playerId: CristalixPlayerId,
    val nickname: String,
    val accountId: UUID?,
    val realm: String,
    val observedAt: Instant,
)
