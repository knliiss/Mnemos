package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
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

        if (context.data == NotificationCallbackData.DROPS) {
            return updateContext(context) {
                NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP_LIST)
            }
        }

        if (context.data == NotificationCallbackData.DELETE_CANCEL) {
            if (!isCurrentMode(context, NotificationCreationMode.ITEM_DROP_DELETE)) {
                return TelegramDeliveryResult.Delivered
            }

            return updateContext(context, NotificationMenuContext::returnToDropList)
        }

        if (context.data == NotificationCallbackData.CREATE) {
            return updateContext(context) {
                NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP)
            }
        }

        if (context.data == NotificationCallbackData.RAID_EDIT) {
            return openRaidEditor(context)
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

        NotificationCallbackData.selectedBoosterTarget(context.data)?.let { target ->
            return creationService.setBooster(
                context = context,
                boosterType = target.value,
                enabled = target.enabled,
            )
        }

        NotificationCallbackData.selectedDungeonTarget(context.data)?.let { target ->
            return creationService.setDungeon(
                context = context,
                dungeonType = target.value,
                enabled = target.enabled,
            )
        }

        NotificationCallbackData.selectedGlobalTarget(context.data)?.let { target ->
            return creationService.setGlobal(
                context = context,
                eventType = target.value,
                enabled = target.enabled,
            )
        }

        NotificationCallbackData.selectedRaidLocationTarget(context.data)?.let { target ->
            return updateCreationContext(context, NotificationCreationMode.RAID) { current ->
                NotificationMenuContext.setRaidLocation(
                    context = current,
                    location = target.location,
                    selected = target.selected,
                )
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

        if (context.data == NotificationCallbackData.RAID_SAVE) {
            return creationService.updateRaid(context)
        }

        NotificationCallbackData.selectedPage(context.data)?.let { page ->
            if (!isCurrentMode(context, NotificationCreationMode.ITEM_DROP_LIST)) {
                return TelegramDeliveryResult.Delivered
            }

            return updateContext(context) { current ->
                NotificationMenuContext.withPage(current, page)
            }
        }

        NotificationCallbackData.confirmedDeleteSubscriptionId(context.data)?.let { subscriptionId ->
            return confirmDelete(context, subscriptionId)
        }

        NotificationCallbackData.deletedSubscriptionId(context.data)?.let { subscriptionId ->
            return requestDelete(context, subscriptionId)
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun openRaidEditor(context: TelegramCallbackContext): TelegramDeliveryResult {
        val result =
            subscriptionGateway.list(
                telegramId = context.telegramId,
                page = 0,
                size = 1,
            )
        val available = result as? NotificationSubscriptionResult.Available
        val locations = available?.raidSubscription?.raidLocations.orEmpty()

        return updateContext(context) {
            NotificationMenuContext.selectRaidMode(locations)
        }
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

    private fun requestDelete(
        context: TelegramCallbackContext,
        subscriptionId: UUID,
    ): TelegramDeliveryResult {
        if (!isCurrentMode(context, NotificationCreationMode.ITEM_DROP_LIST)) {
            return TelegramDeliveryResult.Delivered
        }

        return updateContext(context) { current ->
            NotificationMenuContext.selectDelete(
                context = current,
                subscriptionId = subscriptionId,
            )
        }
    }

    private fun confirmDelete(
        context: TelegramCallbackContext,
        subscriptionId: UUID,
    ): TelegramDeliveryResult {
        val session =
            currentSession(context, NotificationCreationMode.ITEM_DROP_DELETE)
                ?: return TelegramDeliveryResult.Delivered
        val selectedSubscriptionId = NotificationMenuContext.deleteSubscriptionId(session.screenContext)

        if (selectedSubscriptionId != subscriptionId) {
            return TelegramDeliveryResult.Delivered
        }

        subscriptionGateway.delete(
            telegramId = context.telegramId,
            subscriptionId = subscriptionId,
        )

        return updateContext(context, NotificationMenuContext::returnToDropList)
    }

    private fun refresh(context: TelegramCallbackContext): TelegramDeliveryResult = updateContext(context, NotificationMenuContext::refresh)

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
    ): Boolean = currentSession(context, expectedMode) != null

    private fun currentSession(
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

        return session.takeIf { current ->
            NotificationMenuContext.mode(current.screenContext) == expectedMode
        }
    }
}
