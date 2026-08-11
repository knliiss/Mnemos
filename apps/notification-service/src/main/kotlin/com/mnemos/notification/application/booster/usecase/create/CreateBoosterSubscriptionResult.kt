package com.mnemos.notification.application.booster.usecase.create

import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class CreateBoosterSubscriptionResult(
    val subscriptionId: SubscriptionId,
)
