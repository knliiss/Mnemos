package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationCallbackHandler(
    private val menuService: TelegramMenuService,
    private val subscriptionGateway: NotificationSubscriptionGateway,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = NotificationCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (context.data == NotificationCallbackData.REFRESH) {
            return refresh(context)
        }

        NotificationCallbackData.selectedPage(context.data)?.let { page ->
            return menuService.updateScreenContext(
                telegramId = context.telegramId,
                messageId = context.messageId,
                expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
            ) { current ->
                NotificationMenuContext.withPage(current, page)
            }
        }

        NotificationCallbackData.deletedSubscriptionId(context.data)?.let { subscriptionId ->
            return delete(context, subscriptionId)
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun delete(
        context: TelegramCallbackContext,
        subscriptionId: UUID,
    ): TelegramDeliveryResult {
        if (!isCurrentNotificationsMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        subscriptionGateway.delete(
            telegramId = context.telegramId,
            subscriptionId = subscriptionId,
        )

        return refresh(context)
    }

    private fun refresh(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
            update = { current -> current },
        )

    private fun isCurrentNotificationsMenu(context: TelegramCallbackContext): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.NOTIFICATIONS &&
            session.menuMessageId == context.messageId
    }
}
