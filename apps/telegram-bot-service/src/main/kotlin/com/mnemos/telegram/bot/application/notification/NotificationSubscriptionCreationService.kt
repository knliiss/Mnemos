package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationSubscriptionCreationService(
    private val gateway: NotificationSubscriptionCreationGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) {
    fun createItemDrop(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentCreationSession(context, NotificationCreationMode.ITEM_DROP)
            ?: return TelegramDeliveryResult.Delivered
        val draft =
            NotificationItemDropSubscriptionDraft(
                droppedFor = NotificationMenuContext.droppedFor(session.screenContext),
                itemName = NotificationMenuContext.itemName(session.screenContext),
            )

        if (draft.droppedFor == null && draft.itemName == null) {
            return updateFeedback(context, NotificationMenuFeedback.ITEM_DROP_FILTER_REQUIRED)
        }

        return applyResult(
            context = context,
            result = gateway.createItemDrop(context.telegramId, draft),
        )
    }

    fun createBooster(
        context: TelegramCallbackContext,
        boosterType: NotificationBoosterType,
    ): TelegramDeliveryResult =
        createSimple(context, NotificationCreationMode.BOOSTER) {
            gateway.createBooster(context.telegramId, boosterType)
        }

    fun createDungeon(
        context: TelegramCallbackContext,
        dungeonType: NotificationDungeonType,
    ): TelegramDeliveryResult =
        createSimple(context, NotificationCreationMode.DUNGEON) {
            gateway.createDungeon(context.telegramId, dungeonType)
        }

    fun createRaid(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentCreationSession(context, NotificationCreationMode.RAID)
            ?: return TelegramDeliveryResult.Delivered
        val draft =
            NotificationRaidSubscriptionDraft(
                locations = NotificationMenuContext.raidLocations(session.screenContext),
            )

        return applyResult(
            context = context,
            result = gateway.createRaid(context.telegramId, draft),
        )
    }

    fun createGlobal(
        context: TelegramCallbackContext,
        eventType: NotificationGlobalType,
    ): TelegramDeliveryResult =
        createSimple(context, NotificationCreationMode.GLOBAL) {
            gateway.createGlobal(context.telegramId, eventType)
        }

    private fun createSimple(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
        create: () -> NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult {
        currentCreationSession(context, expectedMode)
            ?: return TelegramDeliveryResult.Delivered

        return applyResult(context, create())
    }

    private fun applyResult(
        context: TelegramCallbackContext,
        result: NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult =
        when (result) {
            NotificationSubscriptionCreationResult.APPLIED ->
                menuService.updateScreenContext(
                    telegramId = context.telegramId,
                    messageId = context.messageId,
                    expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
                    update = { NotificationMenuContext.creationSucceeded() },
                )

            NotificationSubscriptionCreationResult.USER_NOT_FOUND ->
                updateFeedback(context, NotificationMenuFeedback.USER_NOT_FOUND)

            NotificationSubscriptionCreationResult.UNAVAILABLE ->
                updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)
        }

    private fun updateFeedback(
        context: TelegramCallbackContext,
        feedback: NotificationMenuFeedback,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
        ) { current ->
            NotificationMenuContext.withFeedback(current, feedback)
        }

    private fun currentCreationSession(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
    ): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.NOTIFICATIONS) {
            return null
        }

        if (session.menuMessageId != context.messageId) {
            return null
        }

        return session.takeIf { current -> NotificationMenuContext.mode(current.screenContext) == expectedMode }
    }
}
