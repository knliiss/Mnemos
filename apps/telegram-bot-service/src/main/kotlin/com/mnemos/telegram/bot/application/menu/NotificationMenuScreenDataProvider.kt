package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.notification.NotificationMenuContext
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropInputField
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.springframework.stereotype.Component

@Component
class NotificationMenuScreenDataProvider(
    private val gateway: NotificationSubscriptionGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.NOTIFICATIONS

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        val mode = NotificationMenuContext.mode(session.screenContext)
        val result =
            if (mode.requiresSubscriptionState()) {
                loadSubscriptions(session, mode)
            } else {
                NotificationSubscriptionResult.NotLoaded
            }

        return TelegramMenuScreenData.Notifications(
            NotificationMenuScreenData(
                result = result,
                mode = mode,
                feedback = NotificationMenuContext.feedback(session.screenContext),
                itemDropInput = resolveItemDropInput(session.pendingInput),
                droppedFor = NotificationMenuContext.droppedFor(session.screenContext),
                itemName = NotificationMenuContext.itemName(session.screenContext),
                deleteSubscriptionId = NotificationMenuContext.deleteSubscriptionId(session.screenContext),
                raidLocations = NotificationMenuContext.raidLocations(session.screenContext),
                raidPage = NotificationMenuContext.raidPage(session.screenContext),
            ),
        )
    }

    private fun loadSubscriptions(
        session: TelegramMenuSession,
        mode: NotificationCreationMode,
    ): NotificationSubscriptionResult {
        val requestedPage =
            if (mode.usesItemDropPage()) {
                NotificationMenuContext.page(session.screenContext)
            } else {
                0
            }
        val result = gateway.list(session.telegramId, requestedPage, PAGE_SIZE)

        if (!mode.usesItemDropPage()) {
            return result
        }

        return resolveValidPage(session.telegramId, requestedPage, result)
    }

    private fun resolveValidPage(
        telegramId: Long,
        requestedPage: Int,
        result: NotificationSubscriptionResult,
    ): NotificationSubscriptionResult {
        val available = result as? NotificationSubscriptionResult.Available ?: return result
        val totalPages = available.page.totalPages

        if (totalPages == 0 || requestedPage < totalPages) {
            return available
        }

        return gateway.list(
            telegramId = telegramId,
            page = totalPages - 1,
            size = PAGE_SIZE,
        )
    }

    private fun NotificationCreationMode.usesItemDropPage(): Boolean =
        this == NotificationCreationMode.ITEM_DROP_LIST ||
            this == NotificationCreationMode.ITEM_DROP_DELETE

    private fun NotificationCreationMode.requiresSubscriptionState(): Boolean =
        when (this) {
            NotificationCreationMode.LIST,
            NotificationCreationMode.GLOBALS,
            NotificationCreationMode.BOOSTERS,
            NotificationCreationMode.DUNGEONS,
            NotificationCreationMode.ITEM_DROP_LIST,
            NotificationCreationMode.ITEM_DROP_DELETE,
            NotificationCreationMode.RAID,
            -> true

            NotificationCreationMode.ITEM_DROP -> false
        }

    private fun resolveItemDropInput(pendingInput: TelegramPendingInput?): NotificationItemDropInputField? =
        when (pendingInput) {
            TelegramPendingInput.NOTIFICATION_ITEM_DROP_PLAYER -> NotificationItemDropInputField.PLAYER
            TelegramPendingInput.NOTIFICATION_ITEM_DROP_ITEM -> NotificationItemDropInputField.ITEM
            else -> null
        }

    private companion object {
        const val PAGE_SIZE = 8
    }
}
