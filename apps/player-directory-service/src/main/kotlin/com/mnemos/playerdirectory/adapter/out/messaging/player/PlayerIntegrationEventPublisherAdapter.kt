package com.mnemos.playerdirectory.adapter.out.messaging.player

import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.playerdirectory.adapter.out.messaging.player.PlayerIntegrationEventMapper.nicknameChanged
import com.mnemos.playerdirectory.adapter.out.messaging.player.PlayerIntegrationEventMapper.presenceChanged
import com.mnemos.playerdirectory.adapter.out.messaging.player.PlayerIntegrationEventMapper.toObservedIntegrationEvent
import com.mnemos.playerdirectory.adapter.out.messaging.player.PlayerIntegrationEventMapper.wentOffline
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.domain.player.model.Player
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PlayerIntegrationEventPublisherAdapter(
    private val publisher: IntegrationEventPublisher,
) : PlayerIntegrationEventPublisher {
    override fun publishInitial(
        player: Player,
        observedAt: Instant,
    ) {
        publisher.publish(player.toObservedIntegrationEvent(observedAt))
    }

    override fun publishChanges(
        previous: Player,
        current: Player,
        observedAt: Instant,
    ) {
        nicknameChanged(previous, current, observedAt)?.let(publisher::publish)
        presenceChanged(previous, current, observedAt)?.let(publisher::publish)
        wentOffline(previous, current, observedAt)?.let(publisher::publish)
    }
}
