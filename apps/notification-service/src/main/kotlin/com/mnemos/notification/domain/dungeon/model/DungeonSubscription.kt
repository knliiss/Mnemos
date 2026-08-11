package com.mnemos.notification.domain.dungeon.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

class DungeonSubscription(
    override val id: SubscriptionId = SubscriptionId.generate(),
    override val userId: UserId,
    val dungeonType: DomainDungeonType,
) : Subscription {
    override val type: SubscriptionType = SubscriptionType.DUNGEON
}
