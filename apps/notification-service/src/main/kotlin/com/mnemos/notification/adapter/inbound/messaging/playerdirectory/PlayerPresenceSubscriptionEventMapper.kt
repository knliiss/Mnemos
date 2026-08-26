package com.mnemos.notification.adapter.inbound.messaging.playerdirectory

import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.playerdirectory.v1.PresenceSubscriptionCreatedV1
import com.mnemos.notification.domain.playerpresence.model.PlayerPresenceSubscription

object PlayerPresenceSubscriptionEventMapper {
    fun PresenceSubscriptionCreatedV1.toDomain(): PlayerPresenceSubscription =
        PlayerPresenceSubscription(
            subscriptionId = subscriptionId,
            subscriberUserId = UserId(subscriberUserId),
            playerId = playerId,
            createdAt = occurredAt,
        )
}
