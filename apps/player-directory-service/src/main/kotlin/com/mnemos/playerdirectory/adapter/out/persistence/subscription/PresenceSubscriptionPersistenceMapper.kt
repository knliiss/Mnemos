package com.mnemos.playerdirectory.adapter.out.persistence.subscription

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscriptionId

object PresenceSubscriptionPersistenceMapper {
    fun PresenceSubscription.toEntity(): PresenceSubscriptionEntity =
        PresenceSubscriptionEntity(
            id = id.id,
            subscriberUserId = subscriberUserId,
            playerId = playerId.id,
            createdAt = createdAt,
        )

    fun PresenceSubscriptionEntity.toDomain(): PresenceSubscription =
        PresenceSubscription(
            id = PresenceSubscriptionId(id),
            subscriberUserId = subscriberUserId,
            playerId = CristalixPlayerId(playerId),
            createdAt = createdAt,
        )
}
