package com.mnemos.playerdirectory.adapter.out.persistence.subscription

import com.mnemos.playerdirectory.adapter.out.persistence.subscription.PresenceSubscriptionPersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.subscription.PresenceSubscriptionPersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PresenceSubscriptionRepositoryAdapter(
    private val repository: PresenceSubscriptionJpaRepository,
) : PresenceSubscriptionRepository {
    override fun save(subscription: PresenceSubscription): PresenceSubscription = repository.save(subscription.toEntity()).toDomain()

    override fun findBySubscriberUserIdAndPlayerId(
        subscriberUserId: UUID,
        playerId: CristalixPlayerId,
    ): PresenceSubscription? = repository.findBySubscriberUserIdAndPlayerId(subscriberUserId, playerId.id)?.toDomain()
}
