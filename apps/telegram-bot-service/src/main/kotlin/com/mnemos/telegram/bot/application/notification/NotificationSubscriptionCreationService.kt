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
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationSubscriptionCreationService(
    private val gateway: NotificationSubscriptionCreationGateway,
    private val subscriptionGateway: NotificationSubscriptionGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) {
    fun createItemDrop(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session =
            currentCreationSession(context, NotificationCreationMode.ITEM_DROP)
                ?: return TelegramDeliveryResult.Delivered
        val draft =
            NotificationItemDropSubscriptionDraft(
                droppedFor = NotificationMenuContext.droppedFor(session.screenContext),
                itemName = NotificationMenuContext.itemName(session.screenContext),
            )

        if (draft.droppedFor == null && draft.itemName == null) {
            return updateFeedback(context, NotificationMenuFeedback.ITEM_DROP_FILTER_REQUIRED)
        }

        return applyCreationResult(
            context = context,
            result = gateway.createItemDrop(context.telegramId, draft),
        )
    }

    fun toggleBooster(
        context: TelegramCallbackContext,
        boosterType: NotificationBoosterType,
    ): TelegramDeliveryResult =
        toggleSimple(
            context = context,
            expectedMode = NotificationCreationMode.BOOSTER,
            existingSubscription = { available -> available.boosterSubscriptions[boosterType] },
            create = { gateway.createBooster(context.telegramId, boosterType) },
        )

    fun toggleDungeon(
        context: TelegramCallbackContext,
        dungeonType: NotificationDungeonType,
    ): TelegramDeliveryResult =
        toggleSimple(
            context = context,
            expectedMode = NotificationCreationMode.DUNGEON,
            existingSubscription = { available -> available.dungeonSubscriptions[dungeonType] },
            create = { gateway.createDungeon(context.telegramId, dungeonType) },
        )

    fun toggleGlobal(
        context: TelegramCallbackContext,
        eventType: NotificationGlobalType,
    ): TelegramDeliveryResult =
        toggleSimple(
            context = context,
            expectedMode = NotificationCreationMode.GLOBAL,
            existingSubscription = { available -> available.globalSubscriptions[eventType] },
            create = { gateway.createGlobal(context.telegramId, eventType) },
        )

    fun updateRaid(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session =
            currentCreationSession(context, NotificationCreationMode.RAID)
                ?: return TelegramDeliveryResult.Delivered
        val state = loadState(context.telegramId) ?: return updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)

        if (state.raidSubscription == null) {
            return updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)
        }

        val draft =
            NotificationRaidSubscriptionDraft(
                locations = NotificationMenuContext.raidLocations(session.screenContext),
            )

        return applyInlineResult(
            context = context,
            result = gateway.updateRaid(context.telegramId, draft),
        )
    }

    private fun toggleSimple(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
        existingSubscription: (NotificationSubscriptionResult.Available) -> UUID?,
        create: () -> NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult {
        currentCreationSession(context, expectedMode)
            ?: return TelegramDeliveryResult.Delivered

        val state = loadState(context.telegramId) ?: return updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)
        val existingId = existingSubscription(state)
        val result =
            if (existingId == null) {
                create()
            } else if (subscriptionGateway.delete(context.telegramId, existingId)) {
                NotificationSubscriptionCreationResult.APPLIED
            } else {
                NotificationSubscriptionCreationResult.UNAVAILABLE
            }

        return applyInlineResult(context, result)
    }

    private fun loadState(telegramId: Long): NotificationSubscriptionResult.Available? =
        subscriptionGateway.list(
            telegramId = telegramId,
            page = 0,
            size = STATE_PAGE_SIZE,
        ) as? NotificationSubscriptionResult.Available

    private fun applyCreationResult(
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

    private fun applyInlineResult(
        context: TelegramCallbackContext,
        result: NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult =
        when (result) {
            NotificationSubscriptionCreationResult.APPLIED ->
                menuService.updateScreenContext(
                    telegramId = context.telegramId,
                    messageId = context.messageId,
                    expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
                    update = NotificationMenuContext::clearFeedback,
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

    private companion object {
        const val STATE_PAGE_SIZE = 1
    }
}
