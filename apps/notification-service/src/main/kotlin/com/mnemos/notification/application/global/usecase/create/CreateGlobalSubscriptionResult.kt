package com.mnemos.notification.application.global.usecase.create

import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class CreateGlobalSubscriptionResult(
    val subscriptionId: SubscriptionId,
)
