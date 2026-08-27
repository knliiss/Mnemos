package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.Player
import java.time.Instant

interface PlayerIntegrationEventPublisher {
    fun publishInitial(
        player: Player,
        observedAt: Instant,
    )

    fun publishChanges(
        previous: Player,
        current: Player,
        observedAt: Instant,
    )
}
