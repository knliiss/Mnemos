package com.mnemos.telegram.bot.application.command

import com.mnemos.telegram.bot.application.command.help.HandleTelegramHelpService
import com.mnemos.telegram.bot.application.command.start.HandleTelegramStartService
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramCommandRouter(
    private val startService: HandleTelegramStartService,
    private val helpService: HandleTelegramHelpService,
    private val menuService: TelegramMenuService,
) {
    fun route(context: TelegramCommandContext): TelegramDeliveryResult {
        val text = context.text.trim()
        val rawCommand = text.substringBefore(' ')
        val command = rawCommand.substringBefore('@').lowercase()

        return when (command) {
            "/start" ->
                startService.handle(
                    TelegramStartContext(
                        telegramId = context.telegramId,
                        firstName = context.firstName,
                        username = context.username,
                        parameter = text.substringAfter(' ', "").trim().takeIf(String::isNotEmpty),
                    ),
                )

            "/help" -> helpService.send(context.telegramId)
            "/menu" -> menuService.open(context.telegramId)
            "/notifications" -> menuService.open(context.telegramId, TelegramMenuScreen.NOTIFICATIONS)
            "/online" -> menuService.open(context.telegramId, TelegramMenuScreen.ONLINE)
            "/events" -> menuService.open(context.telegramId, TelegramMenuScreen.EVENTS)
            "/daily" -> menuService.open(context.telegramId, TelegramMenuScreen.DAILY)
            "/account" -> menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT)
            "/settings" -> menuService.open(context.telegramId, TelegramMenuScreen.SETTINGS)
            else -> TelegramDeliveryResult.Delivered
        }
    }
}

data class TelegramCommandContext(
    val telegramId: Long,
    val text: String,
    val firstName: String?,
    val username: String?,
)
