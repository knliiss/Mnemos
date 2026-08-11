package com.mnemos.notification.application.booster.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.booster.model.DomainBoosterType

data class CreateBoosterSubscriptionCommand(
    val userId: UserId,
    val boosterType: DomainBoosterType,
)
