package com.mnemos.notification.adapter.inbound.http.booster.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionCommand
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionResult

object CreateBoosterSubscriptionApiMapper {
    fun CreateBoosterSubscriptionRequest.toCommand(userId: UserId): CreateBoosterSubscriptionCommand =
        CreateBoosterSubscriptionCommand(
            userId = userId,
            boosterType = boosterType,
        )

    fun CreateBoosterSubscriptionResult.toResponse(): CreateBoosterSubscriptionResponse =
        CreateBoosterSubscriptionResponse(
            subscriptionId = subscriptionId.id,
        )
}
