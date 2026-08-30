package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.notification.NotificationMenuContext
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
        val requestedPage = NotificationMenuContext.page(session.screenContext)
        val result = gateway.list(session.telegramId, requestedPage, PAGE_SIZE)
        val resolvedResult = resolveValidPage(session.telegramId, requestedPage, result)

        return TelegramMenuScreenData.Notifications(
            NotificationMenuScreenData(result = resolvedResult),
        )
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

    private companion object {
        const val PAGE_SIZE = 8
    }
}
