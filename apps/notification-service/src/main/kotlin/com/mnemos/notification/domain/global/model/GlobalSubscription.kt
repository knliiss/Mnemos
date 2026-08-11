package com.mnemos.notification.domain.global.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

class GlobalSubscription(
    override val id: SubscriptionId = SubscriptionId.generate(),
    override val userId: UserId,
    val eventType: DomainGlobalEventType,
) : Subscription {
    override val type: SubscriptionType =
        SubscriptionType.GLOBAL
}
