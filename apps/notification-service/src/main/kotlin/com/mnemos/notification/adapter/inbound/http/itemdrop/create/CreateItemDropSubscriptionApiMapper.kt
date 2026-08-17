package com.mnemos.notification.adapter.inbound.http.itemdrop.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionCommand
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionResult

object CreateItemDropSubscriptionApiMapper {
    fun CreateItemDropSubscriptionRequest.toCommand(userId: UserId): CreateItemDropSubscriptionCommand =
        CreateItemDropSubscriptionCommand(
            userId = userId,
            droppedFor = droppedFor,
            itemName = itemName,
        )

    fun CreateItemDropSubscriptionResult.toResponse(): CreateItemDropSubscriptionResponse =
        CreateItemDropSubscriptionResponse(
            subscriptionId = subscriptionId.id,
            droppedFor = droppedFor,
            itemName = itemName,
        )
}
