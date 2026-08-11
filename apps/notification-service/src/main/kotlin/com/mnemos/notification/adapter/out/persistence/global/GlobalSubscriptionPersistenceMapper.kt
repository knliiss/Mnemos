package com.mnemos.notification.adapter.out.persistence.global

import com.mnemos.notification.domain.global.model.GlobalSubscription

object GlobalSubscriptionPersistenceMapper {
    fun GlobalSubscription.toEntity(): GlobalSubscriptionEntity =
        GlobalSubscriptionEntity(
            subscriptionId = id.id,
            eventType = eventType,
        )

    fun GlobalSubscription.toEquivalenceKey(): String = eventType.name
}
