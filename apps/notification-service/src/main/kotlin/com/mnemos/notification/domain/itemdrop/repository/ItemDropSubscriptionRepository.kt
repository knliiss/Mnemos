package com.mnemos.notification.domain.itemdrop.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.itemdrop.model.ItemDropSubscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId

interface ItemDropSubscriptionRepository {
    fun createOrGetId(subscription: ItemDropSubscription): SubscriptionId

    fun findMatchingUserIds(
        droppedFor: String,
        itemName: String,
    ): Set<UserId>
}
