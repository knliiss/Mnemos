package com.mnemos.playerdirectory.adapter.out.messaging.subscription

import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.playerdirectory.adapter.out.messaging.subscription.PresenceSubscriptionIntegrationEventMapper.toCreatedIntegrationEvent
import com.mnemos.playerdirectory.adapter.out.messaging.subscription.PresenceSubscriptionIntegrationEventMapper.toDeletedIntegrationEvent
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionIntegrationEventPublisher
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PresenceSubscriptionIntegrationEventPublisherAdapter(
    private val publisher: IntegrationEventPublisher,
) : PresenceSubscriptionIntegrationEventPublisher {
    override fun publishCreated(
        subscription: PresenceSubscription,
        occurredAt: Instant,
    ) {
        publisher.publish(subscription.toCreatedIntegrationEvent(occurredAt))
    }

    override fun publishDeleted(
        subscription: PresenceSubscription,
        occurredAt: Instant,
    ) {
        publisher.publish(subscription.toDeletedIntegrationEvent(occurredAt))
    }
}
