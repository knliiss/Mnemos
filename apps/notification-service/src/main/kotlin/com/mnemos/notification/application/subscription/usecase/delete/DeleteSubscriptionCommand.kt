package com.mnemos.notification.application.subscription.usecase.delete

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class DeleteSubscriptionCommand(
    val subscriptionId: SubscriptionId,
    val userId: UserId,
)
