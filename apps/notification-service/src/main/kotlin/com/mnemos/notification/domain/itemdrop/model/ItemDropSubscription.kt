package com.mnemos.notification.domain.itemdrop.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

class ItemDropSubscription(
    override val id: SubscriptionId = SubscriptionId.generate(),
    override val userId: UserId,
    val droppedFor: String?,
    val itemName: String?,
) : Subscription {
    override val type: SubscriptionType =
        SubscriptionType.ITEM_DROP

    init {
        require(droppedFor == null || droppedFor.isNotBlank())
        require(itemName == null || itemName.isNotBlank())
        require(droppedFor != null || itemName != null)
    }
}
