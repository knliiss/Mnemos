package com.mnemos.notification.application.raid.usecase.create

import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class CreateRaidSubscriptionResult(
    val subscriptionId: SubscriptionId,
)
