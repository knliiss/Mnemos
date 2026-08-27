package com.mnemos.playerdirectory.application.accountlink.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Instant
import java.util.UUID

data class PlayerAccountLink(
    val playerId: CristalixPlayerId,
    val accountId: UUID,
    val recordedAt: Instant,
)
