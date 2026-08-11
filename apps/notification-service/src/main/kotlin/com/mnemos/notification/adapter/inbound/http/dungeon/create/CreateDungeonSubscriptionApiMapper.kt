package com.mnemos.notification.adapter.inbound.http.dungeon.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionCommand
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionResult

object CreateDungeonSubscriptionApiMapper {
    fun CreateDungeonSubscriptionRequest.toCommand(userId: UserId): CreateDungeonSubscriptionCommand =
        CreateDungeonSubscriptionCommand(
            userId = userId,
            dungeonType = dungeonType,
        )

    fun CreateDungeonSubscriptionResult.toResponse(): CreateDungeonSubscriptionResponse =
        CreateDungeonSubscriptionResponse(
            subscriptionId = subscriptionId.id,
        )
}
