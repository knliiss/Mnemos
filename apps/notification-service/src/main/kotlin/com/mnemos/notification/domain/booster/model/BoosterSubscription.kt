package com.mnemos.notification.domain.booster.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

class BoosterSubscription(
    override val id: SubscriptionId = SubscriptionId.generate(),
    override val userId: UserId,
    val boosterType: DomainBoosterType,
) : Subscription {
    override val type: SubscriptionType =
        SubscriptionType.BOOSTER
}
