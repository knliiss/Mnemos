package com.mnemos.notification.application.dungeon.usecase.create

import com.mnemos.notification.domain.subscription.model.SubscriptionId

data class CreateDungeonSubscriptionResult(
    val subscriptionId: SubscriptionId,
)
