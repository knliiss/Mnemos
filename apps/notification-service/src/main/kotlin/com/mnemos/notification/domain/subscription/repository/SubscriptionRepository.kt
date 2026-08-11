package com.mnemos.notification.domain.subscription.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.SubscriptionId

interface SubscriptionRepository {
    fun deleteByIdAndUserId(
        subscriptionId: SubscriptionId,
        userId: UserId,
    ): Boolean
}
