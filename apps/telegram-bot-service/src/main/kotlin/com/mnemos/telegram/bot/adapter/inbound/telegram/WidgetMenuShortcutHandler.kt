package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class WidgetMenuShortcutHandler(
    private val menuService: TelegramMenuService,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = "w:open:"

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        val screen =
            when (context.data) {
                "w:open:events" -> TelegramMenuScreen.EVENTS
                "w:open:notifications" -> TelegramMenuScreen.NOTIFICATIONS
                "w:open:account" -> TelegramMenuScreen.ACCOUNT
                "w:open:daily" -> TelegramMenuScreen.DAILY
                else -> return TelegramDeliveryResult.Delivered
            }

        return menuService.open(context.telegramId, screen)
    }
}
