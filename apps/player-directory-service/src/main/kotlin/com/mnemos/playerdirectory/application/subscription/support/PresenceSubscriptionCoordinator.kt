package com.mnemos.playerdirectory.application.subscription.support

import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionIntegrationEventPublisher
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingSchedulePolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PresenceSubscriptionCoordinator(
    private val repository: PresenceSubscriptionRepository,
    private val trackingRepository: PlayerTrackingRepository,
    private val trackingSchedulePolicy: PlayerTrackingSchedulePolicy,
    private val eventPublisher: PresenceSubscriptionIntegrationEventPublisher,
) {
    @Transactional
    fun createIfAbsent(
        subscriberUserId: UUID,
        playerId: CristalixPlayerId,
        createdAt: Instant,
    ): PresenceSubscription {
        val existing =
            repository.findBySubscriberUserIdAndPlayerId(
                subscriberUserId = subscriberUserId,
                playerId = playerId,
            )

        if (existing != null) {
            return existing
        }

        val subscription =
            repository.save(
                PresenceSubscription(
                    subscriberUserId = subscriberUserId,
                    playerId = playerId,
                    createdAt = createdAt,
                ),
            )
        val nextPollAt = trackingSchedulePolicy.nextAfterVerifiedSubscription(createdAt)

        trackingRepository.ensureState(
            PlayerTrackingState.create(
                playerId = playerId,
                nextPollAt = nextPollAt,
                createdAt = createdAt,
            ),
        )

        eventPublisher.publishCreated(
            subscription = subscription,
            occurredAt = createdAt,
        )

        return subscription
    }
}
