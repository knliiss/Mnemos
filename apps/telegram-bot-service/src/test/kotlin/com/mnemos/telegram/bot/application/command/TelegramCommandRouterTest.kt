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
import com.mnemos.telegram.bot.application.widget.dashboard.OpenDashboardWidgetUseCase
import com.mnemos.telegram.bot.application.widget.notifications.OpenNotificationWidgetUseCase
import com.mnemos.telegram.bot.application.widget.events.OpenEventWidgetUseCase
import com.mnemos.telegram.bot.application.widget.accounts.OpenAccountWidgetUseCase
import com.mnemos.telegram.bot.application.widget.daily.OpenDailyWidgetUseCase
import com.mnemos.telegram.bot.application.widget.bets.OpenBetWidgetUseCase
import com.mnemos.telegram.bot.application.widget.wallet.OpenWalletWidgetUseCase
import com.mnemos.telegram.bot.application.widget.online.OpenOnlineWidgetUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class TelegramCommandRouterTest {
    private val startService = mock<HandleTelegramStartService>()
    private val helpService = mock<HandleTelegramHelpService>()
    private val menuService = mock<TelegramMenuService>()
    private val widgetDashboard = mock<OpenDashboardWidgetUseCase>()
    private val notificationWidget = mock<OpenNotificationWidgetUseCase>()
    private val eventWidget = mock<OpenEventWidgetUseCase>()
    private val accountWidget = mock<OpenAccountWidgetUseCase>()
    private val dailyWidget = mock<OpenDailyWidgetUseCase>()
    private val betWidget = mock<OpenBetWidgetUseCase>()
    private val walletWidget = mock<OpenWalletWidgetUseCase>()
    private val onlineWidget = mock<OpenOnlineWidgetUseCase>()
    private val router =
        TelegramCommandRouter(
            startService = startService,
            helpService = helpService,
            menuService = menuService,
            widgetDashboard = widgetDashboard,
            notificationWidget = notificationWidget,
            eventWidget = eventWidget,
            accountWidget = accountWidget,
            dailyWidget = dailyWidget,
            betWidget = betWidget,
            walletWidget = walletWidget,
            onlineWidget = onlineWidget,
        )

    @Test
    fun `start command normalizes bot suffix case and parameter`() {
        val expectedContext =
            TelegramStartContext(
                telegramId = TELEGRAM_ID,
                firstName = "Knalis",
                username = "knalis",
                parameter = "referral-42",
            )
        whenever(startService.handle(expectedContext)).thenReturn(TelegramDeliveryResult.Delivered)

        val result =
            router.route(
                commandContext(
                    text = "  /START@MnemosBot   referral-42  ",
                    firstName = "Knalis",
                    username = "knalis",
                ),
            )

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(startService).handle(expectedContext)
        verifyNoInteractions(helpService, menuService)
    }

    @Test
    fun `help and commands delegate to help service`() {
        whenever(helpService.send(TELEGRAM_ID)).thenReturn(TelegramDeliveryResult.Retryable)

        val helpResult = router.route(commandContext("/help"))
        val commandsResult = router.route(commandContext("/commands"))

        assertEquals(TelegramDeliveryResult.Retryable, helpResult)
        assertEquals(TelegramDeliveryResult.Retryable, commandsResult)
        verify(helpService, times(2)).send(TELEGRAM_ID)
        verifyNoInteractions(startService, menuService)
    }

    @Test
    fun `menu screen shortcut commands open expected screens`() {
        val shortcuts =
            mapOf(
                "/notifications" to TelegramMenuScreen.NOTIFICATIONS,
                "/packs" to TelegramMenuScreen.PACKS,
                "/daily" to TelegramMenuScreen.DAILY,
                "/account" to TelegramMenuScreen.ACCOUNT,
                "/bets" to TelegramMenuScreen.BETS,
                "/prime" to TelegramMenuScreen.PRIME,
                "/settings" to TelegramMenuScreen.SETTINGS,
            )
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        shortcuts.forEach { (command, screen) ->
            val result = router.route(commandContext(command))

            assertEquals(TelegramDeliveryResult.Delivered, result)
            verify(menuService).open(
                telegramId = TELEGRAM_ID,
                screen = screen,
                screenContext = emptyMap(),
                pendingInput = null,
            )
        }

        verifyNoInteractions(startService, helpService)
    }

    @Test
    fun `online numeric argument opens selected realm`() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(commandContext("/online 7"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).open(
            telegramId = TELEGRAM_ID,
            screen = TelegramMenuScreen.ONLINE,
            screenContext = SaoOnlineMenuContext.realm(7),
            pendingInput = null,
        )
    }

    @Test
    fun `online text argument opens player search`() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(commandContext("/online Kirito"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).open(
            telegramId = TELEGRAM_ID,
            screen = TelegramMenuScreen.ONLINE,
            screenContext = SaoOnlineMenuContext.search("Kirito"),
            pendingInput = null,
        )
    }

    @Test
    fun `events argument opens event search`() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(commandContext("/events Secret Sword"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).open(
            telegramId = TELEGRAM_ID,
            screen = TelegramMenuScreen.EVENTS,
            screenContext = EventHistoryMenuContext.search(emptyMap(), "Secret Sword"),
            pendingInput = null,
        )
    }

    @Test
    fun `invalid events argument keeps search input pending with validation feedback`() {
        val invalidQuery = "x".repeat(65)
        val expectedContext =
            EventHistoryMenuContext.withSearchError(
                emptyMap(),
                EventHistorySearchQuery.INVALID_MESSAGE,
            )
        whenever(menuService.open(any(), any(), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(commandContext("/events $invalidQuery"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).open(
            telegramId = TELEGRAM_ID,
            screen = TelegramMenuScreen.EVENTS,
            screenContext = expectedContext,
            pendingInput = TelegramPendingInput.EVENT_SEARCH,
        )
    }

    @Test
    fun `widget shortcut opens dashboard when enabled`() {
        whenever(widgetDashboard.enabled).thenReturn(true)
        whenever(widgetDashboard.execute(TELEGRAM_ID)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/widget")))
        verify(widgetDashboard).execute(TELEGRAM_ID)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `widget shortcut falls back to legacy menu when disabled`() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/widget")))
        verify(menuService).open(
            telegramId = TELEGRAM_ID,
            screen = TelegramMenuScreen.HOME,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun `rejected widget falls back to legacy menu`() {
        whenever(widgetDashboard.enabled).thenReturn(true)
        whenever(widgetDashboard.execute(TELEGRAM_ID)).thenReturn(
            TelegramDeliveryResult.PermanentFailure("telegram-widget-rejected"),
        )
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/widget")))
        verify(menuService).open(
            telegramId = TELEGRAM_ID,
            screen = TelegramMenuScreen.HOME,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun `events command uses event widget without search query`() {
        whenever(eventWidget.enabled).thenReturn(true)
        whenever(eventWidget.execute(TELEGRAM_ID, com.mnemos.telegram.bot.application.events.model.EventHistoryFilter.ALL, null))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/events")))
        verify(eventWidget).execute(
            TELEGRAM_ID,
            com.mnemos.telegram.bot.application.events.model.EventHistoryFilter.ALL,
            null,
        )
        verifyNoInteractions(menuService)
    }

    @Test
    fun `notifications command opens widget when enabled`() {
        whenever(notificationWidget.enabled).thenReturn(true)
        whenever(notificationWidget.execute(TELEGRAM_ID, 0)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/notifications")))
        verify(notificationWidget).execute(TELEGRAM_ID, 0)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `account command opens account widget when enabled`() {
        whenever(accountWidget.enabled).thenReturn(true)
        whenever(accountWidget.execute(TELEGRAM_ID, 0)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/account")))
        verify(accountWidget).execute(TELEGRAM_ID, 0)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `daily command opens daily widget when enabled`() {
        whenever(dailyWidget.enabled).thenReturn(true)
        whenever(dailyWidget.execute(TELEGRAM_ID)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/daily")))
        verify(dailyWidget).execute(TELEGRAM_ID)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `bet command opens read only betting widget when enabled`() {
        whenever(betWidget.enabled).thenReturn(true)
        whenever(betWidget.execute(TELEGRAM_ID, com.mnemos.telegram.bot.application.bet.model.BetMenuMode.ROUNDS))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/bets")))
        verify(betWidget).execute(TELEGRAM_ID, com.mnemos.telegram.bot.application.bet.model.BetMenuMode.ROUNDS)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `wallet command opens widget when enabled`() {
        whenever(walletWidget.enabled).thenReturn(true)
        whenever(walletWidget.execute(TELEGRAM_ID, 0)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/wallet")))
        verify(walletWidget).execute(TELEGRAM_ID, 0)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `online command without query opens widget when enabled`() {
        whenever(onlineWidget.enabled).thenReturn(true)
        whenever(onlineWidget.execute(TELEGRAM_ID, null)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(commandContext("/online")))
        verify(onlineWidget).execute(TELEGRAM_ID, null)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `unknown command is confirmed without side effects`() {
        val result = router.route(commandContext("/unknown"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(startService, helpService, menuService)
    }

    private fun commandContext(
        text: String,
        firstName: String? = null,
        username: String? = null,
    ): TelegramCommandContext =
        TelegramCommandContext(
            telegramId = TELEGRAM_ID,
            text = text,
            firstName = firstName,
            username = username,
        )

    private companion object {
        const val TELEGRAM_ID = 931458258L
    }
}
