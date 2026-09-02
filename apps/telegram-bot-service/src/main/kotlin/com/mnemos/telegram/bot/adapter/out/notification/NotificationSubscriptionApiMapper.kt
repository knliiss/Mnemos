package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramBoosterType
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateBoosterSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateDungeonSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateGlobalSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateItemDropSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateRaidSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramDungeonType
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramGlobalType
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionType

object NotificationSubscriptionApiMapper {
    fun TelegramSubscriptionListResponse.toModel(): NotificationSubscriptionPage =
        NotificationSubscriptionPage(
            items = items.map { subscription -> subscription.toModel() },
            page = page,
            totalPages = totalPages,
            totalItems = totalItems,
            hasNextPage = hasNextPage,
        )

    fun NotificationItemDropSubscriptionDraft.toContract(): TelegramCreateItemDropSubscriptionRequest =
        TelegramCreateItemDropSubscriptionRequest(
            droppedFor = droppedFor,
            itemName = itemName,
        )

    fun NotificationBoosterType.toContract(): TelegramCreateBoosterSubscriptionRequest =
        TelegramCreateBoosterSubscriptionRequest(
            boosterType = TelegramBoosterType.valueOf(name),
        )

    fun NotificationDungeonType.toContract(): TelegramCreateDungeonSubscriptionRequest =
        TelegramCreateDungeonSubscriptionRequest(
            dungeonType = TelegramDungeonType.valueOf(name),
        )

    fun NotificationRaidSubscriptionDraft.toContract(): TelegramCreateRaidSubscriptionRequest =
        TelegramCreateRaidSubscriptionRequest(
            locations = locations,
        )

    fun NotificationGlobalType.toContract(): TelegramCreateGlobalSubscriptionRequest =
        TelegramCreateGlobalSubscriptionRequest(
            eventType = TelegramGlobalType.valueOf(name),
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
