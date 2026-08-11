package com.mnemos.notification.domain.subscription.model

import com.mnemos.common.core.model.id.UserId

interface Subscription {
    val id: SubscriptionId
    val userId: UserId
    val type: SubscriptionType
}
