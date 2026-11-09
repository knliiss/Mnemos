package com.mnemos.telegram.bot.application.command

import com.mnemos.telegram.bot.application.command.help.HandleTelegramHelpService
import com.mnemos.telegram.bot.application.command.start.HandleTelegramStartService
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.EventHistorySearchQuery
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.SaoOnlineSearchQuery
import com.mnemos.telegram.bot.application.widget.dashboard.OpenDashboardWidgetUseCase
import com.mnemos.telegram.bot.application.widget.notifications.OpenNotificationWidgetUseCase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramCommandRouter(
    private val startService: HandleTelegramStartService,
    private val helpService: HandleTelegramHelpService,
    private val menuService: TelegramMenuService,
    private val widgetDashboard: OpenDashboardWidgetUseCase,
    private val notificationWidget: OpenNotificationWidgetUseCase,
) {
    fun route(context: TelegramCommandContext): TelegramDeliveryResult {
        val text = context.text.trim()
        val rawCommand = text.substringBefore(' ')
        val command = rawCommand.substringBefore('@').lowercase()
        val argument = text.substringAfter(' ', "").trim()

        return when (command) {
            "/start" ->
                startService.handle(
                    TelegramStartContext(
                        telegramId = context.telegramId,
                        firstName = context.firstName,
                        username = context.username,
                        parameter = argument.takeIf(String::isNotEmpty),
                    ),
                )

            "/help", "/commands" -> helpService.send(context.telegramId)
            "/menu" -> menuService.open(context.telegramId)
            "/widget" ->
                if (!widgetDashboard.enabled) {
                    menuService.open(context.telegramId)
                } else {
                    when (val result = widgetDashboard.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure -> menuService.open(context.telegramId)
                        else -> result
                    }
                }
            "/notifications" ->
                if (!notificationWidget.enabled) {
                    menuService.open(context.telegramId, TelegramMenuScreen.NOTIFICATIONS)
                } else {
                    when (val result = notificationWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure ->
                            menuService.open(context.telegramId, TelegramMenuScreen.NOTIFICATIONS)
                        else -> result
                    }
                }
            "/packs" -> menuService.open(context.telegramId, TelegramMenuScreen.PACKS)
            "/online" -> openOnline(context.telegramId, argument)
            "/events" -> openEvents(context.telegramId, argument)
            "/daily" -> menuService.open(context.telegramId, TelegramMenuScreen.DAILY)
            "/account" -> menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT)
            "/bets" -> menuService.open(context.telegramId, TelegramMenuScreen.BETS)
            "/prime" -> menuService.open(context.telegramId, TelegramMenuScreen.PRIME)
            "/settings" -> menuService.open(context.telegramId, TelegramMenuScreen.SETTINGS)
            else -> TelegramDeliveryResult.Delivered
        }
    }

    private fun openOnline(
        telegramId: Long,
        argument: String,
    ): TelegramDeliveryResult {
        val realmNumber = argument.toIntOrNull()?.takeIf { number -> number > 0 }
        val searchQuery = SaoOnlineSearchQuery.normalize(argument)
        val screenContext =
            when {
                argument.isBlank() -> emptyMap()
                realmNumber != null -> SaoOnlineMenuContext.realm(realmNumber)
                searchQuery != null -> SaoOnlineMenuContext.search(searchQuery)
                else -> emptyMap()
            }

        return menuService.open(
            telegramId = telegramId,
            screen = TelegramMenuScreen.ONLINE,
            screenContext = screenContext,
        )
    }

    private fun openEvents(
        telegramId: Long,
        argument: String,
    ): TelegramDeliveryResult {
        if (argument.isBlank()) {
            return menuService.open(
                telegramId = telegramId,
                screen = TelegramMenuScreen.EVENTS,
            )
        }

        val searchQuery = EventHistorySearchQuery.normalize(argument)

        if (searchQuery != null) {
            return menuService.open(
                telegramId = telegramId,
                screen = TelegramMenuScreen.EVENTS,
                screenContext = EventHistoryMenuContext.search(emptyMap(), searchQuery),
            )
        }

        return menuService.open(
            telegramId = telegramId,
            screen = TelegramMenuScreen.EVENTS,
            screenContext =
                EventHistoryMenuContext.withSearchError(
                    emptyMap(),
                    EventHistorySearchQuery.INVALID_MESSAGE,
                ),
            pendingInput = TelegramPendingInput.EVENT_SEARCH,
        )
    }
}

data class TelegramCommandContext(
    val telegramId: Long,
    val text: String,
    val firstName: String?,
    val username: String?,
)
