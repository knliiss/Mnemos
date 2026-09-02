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
    private val router =
        TelegramCommandRouter(
            startService = startService,
            helpService = helpService,
            menuService = menuService,
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
