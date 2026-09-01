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

    fun setBooster(
        context: TelegramCallbackContext,
        boosterType: NotificationBoosterType,
        enabled: Boolean,
    ): TelegramDeliveryResult =
        setSimple(
            context = context,
            enabled = enabled,
            existingSubscription = { available -> available.boosterSubscriptions[boosterType] },
            create = { gateway.createBooster(context.telegramId, boosterType) },
        )

    fun setDungeon(
        context: TelegramCallbackContext,
        dungeonType: NotificationDungeonType,
        enabled: Boolean,
    ): TelegramDeliveryResult =
        setSimple(
            context = context,
            enabled = enabled,
            existingSubscription = { available -> available.dungeonSubscriptions[dungeonType] },
            create = { gateway.createDungeon(context.telegramId, dungeonType) },
        )

    fun setGlobal(
        context: TelegramCallbackContext,
        eventType: NotificationGlobalType,
        enabled: Boolean,
    ): TelegramDeliveryResult =
        setSimple(
            context = context,
            enabled = enabled,
            existingSubscription = { available -> available.globalSubscriptions[eventType] },
            create = { gateway.createGlobal(context.telegramId, eventType) },
        )

    fun setRaidLocation(
        context: TelegramCallbackContext,
        location: Int,
        selected: Boolean,
    ): TelegramDeliveryResult {
        val session =
            currentCreationSession(context, NotificationCreationMode.RAID)
                ?: return TelegramDeliveryResult.Delivered
        val currentLocations = NotificationMenuContext.raidLocations(session.screenContext)
        val nextLocations =
            NotificationRaidLocations.withTarget(
                current = currentLocations,
                location = location,
                selected = selected,
            )

        if (nextLocations == currentLocations) {
            return TelegramDeliveryResult.Delivered
        }

        return applyRaidSelection(
            context = context,
            activeLocations = nextLocations,
        )
    }

    fun selectAllRaidLocations(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session =
            currentCreationSession(context, NotificationCreationMode.RAID)
                ?: return TelegramDeliveryResult.Delivered
        val currentLocations = NotificationMenuContext.raidLocations(session.screenContext)

        if (currentLocations == NotificationRaidLocations.all) {
            return TelegramDeliveryResult.Delivered
        }

        return applyRaidSelection(
            context = context,
            activeLocations = NotificationRaidLocations.all,
        )
    }

    fun disableRaid(context: TelegramCallbackContext): TelegramDeliveryResult {
        currentCreationSession(context, NotificationCreationMode.RAID)
            ?: return TelegramDeliveryResult.Delivered

        val state = loadState(context.telegramId) ?: return updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)
        val existingId = state.raidSubscription?.id
        val result =
            when {
                existingId == null -> NotificationSubscriptionCreationResult.APPLIED
                subscriptionGateway.delete(context.telegramId, existingId) -> NotificationSubscriptionCreationResult.APPLIED
                else ->
                    resolveDeleteRace(
                        telegramId = context.telegramId,
                        existingSubscription = { available -> available.raidSubscription?.id },
                    )
            }

        return applyRaidSelectionResult(
            context = context,
            activeLocations = emptySet(),
            result = result,
        )
    }

    fun updateRaid(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session =
            currentCreationSession(context, NotificationCreationMode.RAID)
                ?: return TelegramDeliveryResult.Delivered
        val activeLocations = NotificationMenuContext.raidLocations(session.screenContext)

        if (activeLocations.isEmpty()) {
            return disableRaid(context)
        }

        val draft =
            NotificationRaidSubscriptionDraft(
                locations = NotificationRaidLocations.toBackendLocations(activeLocations),
            )

        return applyRaidUpdateResult(
            context = context,
            result = gateway.updateRaid(context.telegramId, draft),
        )
    }

    private fun setSimple(
        context: TelegramCallbackContext,
        enabled: Boolean,
        existingSubscription: (NotificationSubscriptionResult.Available) -> UUID?,
        create: () -> NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult {
        currentDashboardSession(context)
            ?: return TelegramDeliveryResult.Delivered

        val state = loadState(context.telegramId) ?: return updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)
        val existingId = existingSubscription(state)
        val result =
            when {
                enabled && existingId == null -> create()
                enabled -> NotificationSubscriptionCreationResult.APPLIED
                existingId == null -> NotificationSubscriptionCreationResult.APPLIED
                subscriptionGateway.delete(context.telegramId, existingId) -> NotificationSubscriptionCreationResult.APPLIED
                else -> resolveDeleteRace(context.telegramId, existingSubscription)
            }

        return applyInlineResult(context, result)
    }

    private fun applyRaidSelection(
        context: TelegramCallbackContext,
        activeLocations: Set<Int>,
    ): TelegramDeliveryResult {
        if (activeLocations.isEmpty()) {
            return disableRaid(context)
        }

        val draft =
            NotificationRaidSubscriptionDraft(
                locations = NotificationRaidLocations.toBackendLocations(activeLocations),
            )

        return applyRaidSelectionResult(
            context = context,
            activeLocations = activeLocations,
            result = gateway.updateRaid(context.telegramId, draft),
        )
    }

    private fun resolveDeleteRace(
        telegramId: Long,
        existingSubscription: (NotificationSubscriptionResult.Available) -> UUID?,
    ): NotificationSubscriptionCreationResult {
        val refreshedState = loadState(telegramId) ?: return NotificationSubscriptionCreationResult.UNAVAILABLE

        return if (existingSubscription(refreshedState) == null) {
            NotificationSubscriptionCreationResult.APPLIED
        } else {
            NotificationSubscriptionCreationResult.UNAVAILABLE
        }
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

    private fun applyRaidSelectionResult(
        context: TelegramCallbackContext,
        activeLocations: Set<Int>,
        result: NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult =
        when (result) {
            NotificationSubscriptionCreationResult.APPLIED ->
                menuService.updateScreenContext(
                    telegramId = context.telegramId,
                    messageId = context.messageId,
                    expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
                ) { current ->
                    NotificationMenuContext.replaceRaidLocations(
                        context = current,
                        activeLocations = activeLocations,
                    )
                }

            NotificationSubscriptionCreationResult.USER_NOT_FOUND ->
                updateFeedback(context, NotificationMenuFeedback.USER_NOT_FOUND)

            NotificationSubscriptionCreationResult.UNAVAILABLE ->
                updateFeedback(context, NotificationMenuFeedback.SERVICE_UNAVAILABLE)
        }

    private fun applyRaidUpdateResult(
        context: TelegramCallbackContext,
        result: NotificationSubscriptionCreationResult,
    ): TelegramDeliveryResult =
        when (result) {
            NotificationSubscriptionCreationResult.APPLIED ->
                menuService.updateScreenContext(
                    telegramId = context.telegramId,
                    messageId = context.messageId,
                    expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
                    update = { NotificationMenuContext.raidUpdateSucceeded() },
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

    private fun currentDashboardSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = currentSession(context) ?: return null

        return session.takeIf { current ->
            NotificationMenuContext.mode(current.screenContext) == NotificationCreationMode.LIST
        }
    }

    private fun currentCreationSession(
        context: TelegramCallbackContext,
        expectedMode: NotificationCreationMode,
    ): TelegramMenuSession? {
        val session = currentSession(context) ?: return null

        return session.takeIf { current ->
            NotificationMenuContext.mode(current.screenContext) == expectedMode
        }
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

    private companion object {
        const val STATE_PAGE_SIZE = 1
    }
}
