package com.mnemos.playerdirectory.application.tracking.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Instant

data class PlayerTrackingLease(
    val playerId: CristalixPlayerId,
    val leaseUntil: Instant,
)
