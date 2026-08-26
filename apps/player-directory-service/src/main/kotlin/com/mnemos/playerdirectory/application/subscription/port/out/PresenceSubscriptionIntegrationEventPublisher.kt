package com.mnemos.playerdirectory.application.subscription.port.out

import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import java.time.Instant

interface PresenceSubscriptionIntegrationEventPublisher {
    fun publishCreated(
        subscription: PresenceSubscription,
        occurredAt: Instant,
    )

    fun publishDeleted(
        subscription: PresenceSubscription,
        occurredAt: Instant,
    )
}
