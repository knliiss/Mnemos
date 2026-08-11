package com.mnemos.notification.application.itemdrop.usecase.create

import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class CreateItemDropSubscriptionResult(
    val subscriptionId: SubscriptionId,
)
