package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationCallbackHandler(
    private val menuService: TelegramMenuService,
    private val subscriptionGateway: NotificationSubscriptionGateway,
    private val creationService: NotificationSubscriptionCreationService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = NotificationCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (context.data == NotificationCallbackData.REFRESH) {
            return refresh(context)
        }

        if (context.data == NotificationCallbackData.CREATE) {
            return updateContext(context) { NotificationMenuContext.startCreation() }
        }

        if (context.data == NotificationCallbackData.CREATE_TYPES) {
            return updateContext(context) { NotificationMenuContext.startCreation() }
        }

        NotificationCallbackData.selectedCreationMode(context.data)?.let { mode ->
            return updateContext(context) { NotificationMenuContext.selectMode(mode) }
        }

        if (context.data == NotificationCallbackData.ITEM_DROP_PLAYER) {
            return requestItemDropInput(
                context = context,
                pendingInput = TelegramPendingInput.NOTIFICATION_ITEM_DROP_PLAYER,
            )
        }

        if (context.data == NotificationCallbackData.ITEM_DROP_ITEM) {
            return requestItemDropInput(
                context = context,
                pendingInput = TelegramPendingInput.NOTIFICATION_ITEM_DROP_ITEM,
            )
        }

        if (context.data == NotificationCallbackData.ITEM_DROP_INPUT_CANCEL) {
            if (!isCurrentMode(context, NotificationCreationMode.ITEM_DROP)) {
                return TelegramDeliveryResult.Delivered
            }

            return menuService.cancelTextInput(
                telegramId = context.telegramId,
                messageId = context.messageId,
                expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
                updateContext = NotificationMenuContext::clearFeedback,
            )
        }

        if (context.data == NotificationCallbackData.ITEM_DROP_CLEAR_PLAYER) {
            return updateCreationContext(
                context,
                NotificationCreationMode.ITEM_DROP,
                NotificationMenuContext::withoutDroppedFor,
            )
        }

        if (context.data == NotificationCallbackData.ITEM_DROP_CLEAR_ITEM) {
            return updateCreationContext(
                context,
                NotificationCreationMode.ITEM_DROP,
                NotificationMenuContext::withoutItemName,
            )
        }

        if (context.data == NotificationCallbackData.ITEM_DROP_CREATE) {
            return creationService.createItemDrop(context)
        }

        NotificationCallbackData.selectedBoosterType(context.data)?.let { boosterType ->
            return creationService.createBooster(context, boosterType)
        }

        NotificationCallbackData.selectedDungeonType(context.data)?.let { dungeonType ->
            return creationService.createDungeon(context, dungeonType)
        }

        NotificationCallbackData.selectedGlobalType(context.data)?.let { globalType ->
            return creationService.createGlobal(context, globalType)
        }

        NotificationCallbackData.selectedRaidLocation(context.data)?.let { location ->
            return updateCreationContext(context, NotificationCreationMode.RAID) { current ->
                NotificationMenuContext.toggleRaidLocation(current, location)
            }
        }

        NotificationCallbackData.selectedRaidPage(context.data)?.let { page ->
            return updateCreationContext(context, NotificationCreationMode.RAID) { current ->
                NotificationMenuContext.withRaidPage(current, page)
            }
        }

        if (context.data == NotificationCallbackData.RAID_CLEAR) {
            return updateCreationContext(
                context,
                NotificationCreationMode.RAID,
                NotificationMenuContext::clearRaidLocations,
            )
        }

        if (context.data == NotificationCallbackData.RAID_CREATE) {
            return creationService.createRaid(context)
        }

        NotificationCallbackData.selectedPage(context.data)?.let { page ->
            return updateContext(context) { current ->
                NotificationMenuContext.withPage(current, page)
            }
        }

        NotificationCallbackData.deletedSubscriptionId(context.data)?.let { subscriptionId ->
            return delete(context, subscriptionId)
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun requestItemDropInput(
        context: TelegramCallbackContext,
        pendingInput: TelegramPendingInput,
    ): TelegramDeliveryResult {
        if (!isCurrentMode(context, NotificationCreationMode.ITEM_DROP)) {
            return TelegramDeliveryResult.Delivered
        }

        return menuService.requestTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
            pendingInput = pendingInput,
            updateContext = NotificationMenuContext::clearFeedback,
        )
    }

    private fun delete(
        context: TelegramCallbackContext,
        subscriptionId: UUID,
    ): TelegramDeliveryResult {
        if (!isCurrentMode(context, NotificationCreationMode.LIST)) {
            return TelegramDeliveryResult.Delivered
        }

        subscriptionGateway.delete(
            telegramId = context.telegramId,
            subscriptionId = subscriptionId,
        )

        return refresh(context)
    }

    private fun refresh(context: TelegramCallbackContext): TelegramDeliveryResult =
        updateContext(context, NotificationMenuContext::refresh)

    private fun updateCreationContext(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult {
        if (!isCurrentMode(context, expectedMode)) {
            return TelegramDeliveryResult.Delivered
        }

        return updateContext(context, update)
    }

    private fun updateContext(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
            update = update,
        )

    private fun isCurrentMode(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
    ): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.NOTIFICATIONS &&
            session.menuMessageId == context.messageId &&
            NotificationMenuContext.mode(session.screenContext) == expectedMode
    }
}
