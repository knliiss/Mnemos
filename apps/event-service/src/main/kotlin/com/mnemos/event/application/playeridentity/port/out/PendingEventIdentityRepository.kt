package com.mnemos.event.application.playeridentity.port.out

import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Duration
import java.time.Instant

interface PendingEventIdentityRepository {
    fun reconcile(
        nickname: String,
        playerId: CristalixPlayerId,
        observedAt: Instant,
        maxAge: Duration,
    )
}
