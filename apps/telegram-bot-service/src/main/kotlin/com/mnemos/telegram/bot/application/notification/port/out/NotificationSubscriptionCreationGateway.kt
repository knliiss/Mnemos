package com.mnemos.telegram.bot.application.notification.port.out

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult

interface NotificationSubscriptionCreationGateway {
    fun createItemDrop(
        telegramId: Long,
        draft: NotificationItemDropSubscriptionDraft,
    ): NotificationSubscriptionCreationResult

    fun createBooster(
        telegramId: Long,
        boosterType: NotificationBoosterType,
    ): NotificationSubscriptionCreationResult

    fun createDungeon(
        telegramId: Long,
        dungeonType: NotificationDungeonType,
    ): NotificationSubscriptionCreationResult

    fun createRaid(
        telegramId: Long,
        draft: NotificationRaidSubscriptionDraft,
    ): NotificationSubscriptionCreationResult

    fun updateRaid(
        telegramId: Long,
        draft: NotificationRaidSubscriptionDraft,
    ): NotificationSubscriptionCreationResult = NotificationSubscriptionCreationResult.UNAVAILABLE

    fun createGlobal(
        telegramId: Long,
        eventType: NotificationGlobalType,
    ): NotificationSubscriptionCreationResult
}
