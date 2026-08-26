package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.Player
import java.time.Instant

fun interface PlayerIntegrationEventPublisher {
    fun publishChanges(
        previous: Player,
        current: Player,
        observedAt: Instant,
    )
}
