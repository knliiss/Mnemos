package com.mnemos.playerdirectory.adapter.out.messaging.subscription

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PresenceSubscriptionCreatedV1
import com.mnemos.contracts.playerdirectory.v1.PresenceSubscriptionDeletedV1
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import java.time.Instant

object PresenceSubscriptionIntegrationEventMapper {
    fun PresenceSubscription.toCreatedIntegrationEvent(occurredAt: Instant): IntegrationEvent<PresenceSubscriptionCreatedV1> =
        IntegrationEvent(
            type = PlayerDirectoryEventType.PRESENCE_SUBSCRIPTION_CREATED,
            aggregateId = playerId.id,
            occurredAt = occurredAt,
            payload =
                PresenceSubscriptionCreatedV1(
                    subscriptionId = id.id,
                    subscriberUserId = subscriberUserId,
                    playerId = playerId.id,
                    occurredAt = occurredAt,
                ),
        )

    fun PresenceSubscription.toDeletedIntegrationEvent(occurredAt: Instant): IntegrationEvent<PresenceSubscriptionDeletedV1> =
        IntegrationEvent(
            type = PlayerDirectoryEventType.PRESENCE_SUBSCRIPTION_DELETED,
            aggregateId = playerId.id,
            occurredAt = occurredAt,
            payload =
                PresenceSubscriptionDeletedV1(
                    subscriptionId = id.id,
                    subscriberUserId = subscriberUserId,
                    playerId = playerId.id,
                    occurredAt = occurredAt,
                ),
        )
}
