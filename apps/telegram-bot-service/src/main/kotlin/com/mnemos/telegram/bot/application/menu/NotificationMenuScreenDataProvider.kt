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
            if (mode == NotificationCreationMode.LIST) {
                loadSubscriptions(session)
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
                raidLocations = NotificationMenuContext.raidLocations(session.screenContext),
                raidPage = NotificationMenuContext.raidPage(session.screenContext),
            ),
        )
    }

    private fun loadSubscriptions(session: TelegramMenuSession): NotificationSubscriptionResult {
        val requestedPage = NotificationMenuContext.page(session.screenContext)
        val result = gateway.list(session.telegramId, requestedPage, PAGE_SIZE)

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
