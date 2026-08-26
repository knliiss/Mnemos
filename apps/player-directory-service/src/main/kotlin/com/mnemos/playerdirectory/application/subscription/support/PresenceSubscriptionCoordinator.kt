package com.mnemos.playerdirectory.application.subscription.support

import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PresenceSubscriptionCoordinator(
    private val repository: PresenceSubscriptionRepository,
) {
    @Transactional
    fun createIfAbsent(
        subscriberUserId: UUID,
        playerId: CristalixPlayerId,
        createdAt: Instant,
    ): PresenceSubscription =
        repository.findBySubscriberUserIdAndPlayerId(
            subscriberUserId = subscriberUserId,
            playerId = playerId,
        ) ?: repository.save(
            PresenceSubscription(
                subscriberUserId = subscriberUserId,
                playerId = playerId,
                createdAt = createdAt,
            ),
        )
}
