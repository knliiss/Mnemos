package com.mnemos.notification.adapter.inbound.http.raid.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionResult

object CreateRaidSubscriptionApiMapper {
    fun CreateRaidSubscriptionRequest.toCommand(userId: UserId): CreateRaidSubscriptionCommand =
        CreateRaidSubscriptionCommand(
            userId = userId,
            locations = locations,
        )

    fun CreateRaidSubscriptionResult.toResponse(): CreateRaidSubscriptionResponse =
        CreateRaidSubscriptionResponse(
            subscriptionId = subscriptionId.id,
        )
}
