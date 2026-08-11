package com.mnemos.notification.adapter.inbound.http.booster.create

import com.mnemos.notification.domain.booster.model.DomainBoosterType

data class CreateBoosterSubscriptionRequest(
    val boosterType: DomainBoosterType,
)
