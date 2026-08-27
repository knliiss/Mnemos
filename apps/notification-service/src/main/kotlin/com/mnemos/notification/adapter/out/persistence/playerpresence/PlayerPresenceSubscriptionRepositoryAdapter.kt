package com.mnemos.notification.adapter.out.persistence.playerpresence

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.playerpresence.port.out.PlayerPresenceSubscriptionRepository
import com.mnemos.notification.domain.playerpresence.model.PlayerPresenceSubscription
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PlayerPresenceSubscriptionRepositoryAdapter(
    private val repository: PlayerPresenceSubscriptionJpaRepository,
) : PlayerPresenceSubscriptionRepository {
    override fun ensure(subscription: PlayerPresenceSubscription) {
        repository.insertIfAbsent(
            subscriptionId = subscription.subscriptionId,
            subscriberUserId = subscription.subscriberUserId.id,
            playerId = subscription.playerId,
            createdAt = subscription.createdAt,
        )
    }

    override fun delete(subscriptionId: UUID) {
        repository.deleteById(subscriptionId)
    }

    override fun findSubscriberUserIds(playerId: UUID): Set<UserId> =
        repository
            .findSubscriberUserIdsByPlayerId(playerId)
            .map(::UserId)
            .toSet()
}
