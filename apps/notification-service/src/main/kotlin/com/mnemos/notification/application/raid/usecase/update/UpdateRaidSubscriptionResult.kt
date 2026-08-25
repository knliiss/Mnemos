package com.mnemos.notification.application.raid.usecase.update

import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class UpdateRaidSubscriptionResult(
    val subscriptionId: SubscriptionId,
)
