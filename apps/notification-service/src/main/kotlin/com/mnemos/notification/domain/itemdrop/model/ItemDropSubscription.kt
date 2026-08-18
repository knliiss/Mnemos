package com.mnemos.notification.domain.itemdrop.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

class ItemDropSubscription(
    override val id: SubscriptionId = SubscriptionId.generate(),
    override val userId: UserId,
    droppedFor: String?,
    itemName: String?,
) : Subscription {
    val droppedFor: String? = droppedFor?.trim()
    val itemName: String? = itemName?.trim()

    override val type: SubscriptionType = SubscriptionType.ITEM_DROP

    init {
        require(this.droppedFor == null || this.droppedFor.isNotBlank())
        require(this.itemName == null || this.itemName.isNotBlank())
        require(this.droppedFor != null || this.itemName != null)
    }
}
