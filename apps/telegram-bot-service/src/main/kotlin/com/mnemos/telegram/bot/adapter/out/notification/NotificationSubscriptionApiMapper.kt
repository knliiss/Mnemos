package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionType

object NotificationSubscriptionApiMapper {
    fun TelegramSubscriptionListResponse.toModel(): NotificationSubscriptionPage =
        NotificationSubscriptionPage(
            items = items.map(TelegramSubscriptionResponse::toModel),
            page = page,
            totalPages = totalPages,
            totalItems = totalItems,
            hasNextPage = hasNextPage,
        )

    private fun TelegramSubscriptionResponse.toModel(): NotificationSubscription =
        NotificationSubscription(
            id = id,
            type = type.toModel(),
            createdAt = createdAt,
            droppedFor = droppedFor,
            itemName = itemName,
            boosterType = boosterType,
            dungeonType = dungeonType,
            globalType = globalType,
            raidLocations = raidLocations,
        )

    private fun TelegramSubscriptionType.toModel(): NotificationSubscriptionType = NotificationSubscriptionType.valueOf(name)
}
