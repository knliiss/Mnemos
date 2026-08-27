package com.mnemos.playerdirectory.application.subscription.usecase.delete

import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionIntegrationEventPublisher
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class DeletePresenceSubscriptionService(
    private val repository: PresenceSubscriptionRepository,
    private val eventPublisher: PresenceSubscriptionIntegrationEventPublisher,
    private val clock: Clock,
) : DeletePresenceSubscriptionUseCase {
    @Transactional
    override fun execute(
        subscriberUserId: UUID,
        playerId: CristalixPlayerId,
    ) {
        val subscription = repository.findBySubscriberUserIdAndPlayerId(subscriberUserId, playerId) ?: return
        val deletedAt = Instant.now(clock)

        repository.delete(subscription)

        eventPublisher.publishDeleted(
            subscription = subscription,
            occurredAt = deletedAt,
        )
    }
}
