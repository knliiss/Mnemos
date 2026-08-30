package com.mnemos.telegram.bot.application.callback

import com.mnemos.telegram.bot.application.command.help.HandleTelegramHelpService
import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramCallbackRouter(
    private val helpService: HandleTelegramHelpService,
    private val menuService: TelegramMenuService,
) {
    fun route(context: TelegramCallbackContext): TelegramDeliveryResult {
        TelegramHelpSection.fromCallbackData(context.data)?.let { section ->
            return helpService.switchSection(
                telegramId = context.telegramId,
                messageId = context.messageId,
                section = section,
            )
        }

        if (context.data == TelegramMenuScreen.BACK_CALLBACK_DATA) {
            return menuService.back(
                telegramId = context.telegramId,
                messageId = context.messageId,
            )
        }

        val screen =
            TelegramMenuScreen.fromCallbackData(context.data)
                ?: return TelegramDeliveryResult.Delivered

        return menuService.navigate(
            telegramId = context.telegramId,
            messageId = context.messageId,
            screen = screen,
        )
    }
}

data class TelegramCallbackContext(
    val telegramId: Long,
    val messageId: Int,
    val data: String,
)
