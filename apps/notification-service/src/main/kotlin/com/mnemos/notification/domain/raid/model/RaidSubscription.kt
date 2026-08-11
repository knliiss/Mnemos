package com.mnemos.notification.domain.raid.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

class RaidSubscription(
    override val id: SubscriptionId = SubscriptionId.generate(),
    override val userId: UserId,
    val locations: Set<Int>,
) : Subscription {
    override val type = SubscriptionType.RAID
}
