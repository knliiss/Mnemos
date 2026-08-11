package com.mnemos.notification.adapter.inbound.http.global.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionCommand
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionResult

object CreateGlobalSubscriptionApiMapper {
    fun CreateGlobalSubscriptionRequest.toCommand(userId: UserId): CreateGlobalSubscriptionCommand =
        CreateGlobalSubscriptionCommand(
            userId = userId,
            eventType = eventType,
        )

    fun CreateGlobalSubscriptionResult.toResponse(): CreateGlobalSubscriptionResponse =
        CreateGlobalSubscriptionResponse(
            subscriptionId = subscriptionId.id,
        )
}
