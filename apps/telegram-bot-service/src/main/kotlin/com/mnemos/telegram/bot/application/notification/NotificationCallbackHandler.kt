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
        val session = currentSession(context) ?: return TelegramDeliveryResult.Delivered

        if (isItemDropInput(session.pendingInput)) {
            return if (context.data == NotificationCallbackData.ITEM_DROP_INPUT_CANCEL) {
                cancelItemDropInput(context)
            } else {
                TelegramDeliveryResult.Delivered
            }
        }

        if (context.data == NotificationCallbackData.REFRESH) {
            return refresh(context)
        }

        if (context.data == NotificationCallbackData.GLOBALS) {
            return openCategory(context, NotificationCreationMode.GLOBALS)
        }

        if (context.data == NotificationCallbackData.BOOSTERS) {
            return openCategory(context, NotificationCreationMode.BOOSTERS)
        }

        if (context.data == NotificationCallbackData.DUNGEONS) {
            return openCategory(context, NotificationCreationMode.DUNGEONS)
        }

        if (context.data == NotificationCallbackData.DROPS) {
            if (!isCurrentMode(context, NotificationCreationMode.LIST, NotificationCreationMode.ITEM_DROP)) {
                return TelegramDeliveryResult.Delivered
            }

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
            if (!isCurrentMode(context, NotificationCreationMode.ITEM_DROP_LIST)) {
                return TelegramDeliveryResult.Delivered
            }

            return updateContext(context) {
                NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP)
            }
        }

        if (context.data == NotificationCallbackData.RAID_EDIT) {
            if (!isCurrentMode(context, NotificationCreationMode.LIST)) {
                return TelegramDeliveryResult.Delivered
            }

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
            return TelegramDeliveryResult.Delivered
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
            if (!isCurrentMode(context, NotificationCreationMode.BOOSTERS)) {
                return TelegramDeliveryResult.Delivered
            }

            return creationService.setBooster(
                context = context,
                boosterType = target.value,
                enabled = target.enabled,
            )
        }

        NotificationCallbackData.selectedDungeonTarget(context.data)?.let { target ->
            if (!isCurrentMode(context, NotificationCreationMode.DUNGEONS)) {
                return TelegramDeliveryResult.Delivered
            }

            return creationService.setDungeon(
                context = context,
                dungeonType = target.value,
                enabled = target.enabled,
            )
        }

        NotificationCallbackData.selectedGlobalTarget(context.data)?.let { target ->
            if (!isCurrentMode(context, NotificationCreationMode.GLOBALS)) {
                return TelegramDeliveryResult.Delivered
            }

            return creationService.setGlobal(
                context = context,
                eventType = target.value,
                enabled = target.enabled,
            )
        }

        NotificationCallbackData.selectedRaidLocationTarget(context.data)?.let { target ->
            return creationService.setRaidLocation(
                context = context,
                location = target.location,
                selected = target.selected,
            )
        }

        NotificationCallbackData.selectedRaidPage(context.data)?.let { page ->
            return updateCreationContext(context, NotificationCreationMode.RAID) { current ->
                NotificationMenuContext.withRaidPage(current, page)
            }
        }

        if (context.data == NotificationCallbackData.RAID_DISABLE) {
            return creationService.disableRaid(context)
        }

        if (context.data == NotificationCallbackData.RAID_CLEAR) {
            return creationService.selectAllRaidLocations(context)
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

    private fun openCategory(
        context: TelegramCallbackContext,
        mode: NotificationCreationMode,
    ): TelegramDeliveryResult {
        if (!isCurrentMode(context, NotificationCreationMode.LIST)) {
            return TelegramDeliveryResult.Delivered
        }

        return updateContext(context) {
            NotificationMenuContext.selectMode(mode)
        }
    }

    private fun openRaidEditor(context: TelegramCallbackContext): TelegramDeliveryResult {
        val result =
            subscriptionGateway.list(
                telegramId = context.telegramId,
                page = 0,
                size = 1,
            )
        val available = result as? NotificationSubscriptionResult.Available
        val activeLocations = NotificationRaidLocations.active(available?.raidSubscription)

        return updateContext(context) {
            NotificationMenuContext.selectRaidMode(activeLocations)
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

    private fun cancelItemDropInput(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
            updateContext = NotificationMenuContext::clearFeedback,
        )

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
        vararg expectedModes: NotificationCreationMode,
    ): Boolean {
        val session = currentSession(context) ?: return false
        val currentMode = NotificationMenuContext.mode(session.screenContext)

        return currentMode in expectedModes
    }

    private fun currentSession(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
    ): TelegramMenuSession? =
        currentSession(context)?.takeIf { current ->
            NotificationMenuContext.mode(current.screenContext) == expectedMode
        }

    private fun currentSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.NOTIFICATIONS) {
            return null
        }

        if (session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }

    private fun isItemDropInput(pendingInput: TelegramPendingInput?): Boolean =
        pendingInput == TelegramPendingInput.NOTIFICATION_ITEM_DROP_PLAYER ||
            pendingInput == TelegramPendingInput.NOTIFICATION_ITEM_DROP_ITEM
}
