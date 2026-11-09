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
import com.mnemos.telegram.bot.application.widget.events.OpenEventWidgetUseCase
import com.mnemos.telegram.bot.application.widget.accounts.OpenAccountWidgetUseCase
import com.mnemos.telegram.bot.application.widget.daily.OpenDailyWidgetUseCase
import com.mnemos.telegram.bot.application.widget.bets.OpenBetWidgetUseCase
import com.mnemos.telegram.bot.application.widget.wallet.OpenWalletWidgetUseCase
import com.mnemos.telegram.bot.application.widget.online.OpenOnlineWidgetUseCase
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
    private val eventWidget: OpenEventWidgetUseCase,
    private val accountWidget: OpenAccountWidgetUseCase,
    private val dailyWidget: OpenDailyWidgetUseCase,
    private val betWidget: OpenBetWidgetUseCase,
    private val walletWidget: OpenWalletWidgetUseCase,
    private val onlineWidget: OpenOnlineWidgetUseCase,
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
            "/online" ->
                if (argument.isBlank() && onlineWidget.enabled) {
                    when (val result = onlineWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure -> openOnline(context.telegramId, argument)
                        else -> result
                    }
                } else {
                    openOnline(context.telegramId, argument)
                }
            "/events" ->
                if (argument.isBlank() && eventWidget.enabled) {
                    when (val result = eventWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure -> openEvents(context.telegramId, argument)
                        else -> result
                    }
                } else {
                    openEvents(context.telegramId, argument)
                }
            "/daily" ->
                if (dailyWidget.enabled) {
                    when (val result = dailyWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure ->
                            menuService.open(context.telegramId, TelegramMenuScreen.DAILY)
                        else -> result
                    }
                } else {
                    menuService.open(context.telegramId, TelegramMenuScreen.DAILY)
                }
            "/account" ->
                if (accountWidget.enabled) {
                    when (val result = accountWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure ->
                            menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT)
                        else -> result
                    }
                } else {
                    menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT)
                }
            "/bets" ->
                if (betWidget.enabled) {
                    when (val result = betWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure ->
                            menuService.open(context.telegramId, TelegramMenuScreen.BETS)
                        else -> result
                    }
                } else {
                    menuService.open(context.telegramId, TelegramMenuScreen.BETS)
                }
            "/wallet" ->
                if (walletWidget.enabled) {
                    when (val result = walletWidget.execute(context.telegramId)) {
                        is TelegramDeliveryResult.PermanentFailure ->
                            menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT)
                        else -> result
                    }
                } else {
                    menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT)
                }
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

