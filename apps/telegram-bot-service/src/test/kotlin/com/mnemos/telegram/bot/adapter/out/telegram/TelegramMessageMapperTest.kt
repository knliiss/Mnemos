package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.adapter.out.telegram.TelegramMessageMapper.toReplyMarkup
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class TelegramMessageMapperTest {
    @Test
    fun `mini app action uses primary inline web app button`() {
        val message =
            OutboundTelegramMessage(
                text = "open app",
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actions =
                    listOf(
                        OutboundTelegramMessageAction.OpenMiniApp(
                            label = "Open Mnemos",
                        ),
                    ),
            )

        val markup = message.toReplyMarkup(MINI_APP_URL)
        val inlineMarkup = assertInstanceOf(InlineKeyboardMarkup::class.java, markup)
        val button = inlineMarkup.inlineKeyboard().single().single()

        assertEquals("Open Mnemos", button.text)
        assertEquals(MINI_APP_URL, button.webApp?.url())
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, message.actions.single().style)
        assertEquals(true, message.actions.single().accented)
    }

    @Test
    fun `explicit styled callback remains styled without accent opt in`() {
        val action =
            OutboundTelegramMessageAction.Callback(
                label = "Active",
                data = "m:active",
                style = OutboundTelegramButtonStyle.SUCCESS,
            )
        val message =
            OutboundTelegramMessage(
                text = "menu",
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actions = listOf(action),
            )

        val markup = message.toReplyMarkup(MINI_APP_URL)
        val inlineMarkup = assertInstanceOf(InlineKeyboardMarkup::class.java, markup)
        val button = inlineMarkup.inlineKeyboard().single().single()

        assertEquals("m:active", button.callbackData)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, action.style)
        assertEquals(false, action.accented)
    }

    @Test
    fun `legacy callback actions use adaptive compact rows`() {
        val message =
            OutboundTelegramMessage(
                text = "help",
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actions =
                    listOf(
                        OutboundTelegramMessageAction.OpenMiniApp("Open Mnemos"),
                        OutboundTelegramMessageAction.Callback("Overview", "help:overview"),
                        OutboundTelegramMessageAction.Callback("Accounts", "help:accounts"),
                        OutboundTelegramMessageAction.Callback("Problems", "help:troubleshooting"),
                    ),
            )

        val markup = message.toReplyMarkup(MINI_APP_URL)
        val inlineMarkup = assertInstanceOf(InlineKeyboardMarkup::class.java, markup)
        val rows = inlineMarkup.inlineKeyboard()

        assertEquals(2, rows.size)
        assertEquals(1, rows[0].size)
        assertEquals(3, rows[1].size)
        assertEquals("help:overview", rows[1][0].callbackData)
        assertEquals("help:accounts", rows[1][1].callbackData)
        assertEquals("help:troubleshooting", rows[1][2].callbackData)
    }

    @Test
    fun `explicit action rows are preserved exactly`() {
        val message =
            OutboundTelegramMessage(
                text = "menu",
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actionRows =
                    listOf(
                        listOf(
                            OutboundTelegramMessageAction.Callback("One", "m:one"),
                            OutboundTelegramMessageAction.Callback("Two", "m:two"),
                            OutboundTelegramMessageAction.Callback("Three", "m:three"),
                        ),
                        listOf(
                            OutboundTelegramMessageAction.Callback("Four", "m:four"),
                        ),
                    ),
            )

        val markup = message.toReplyMarkup(MINI_APP_URL)
        val inlineMarkup = assertInstanceOf(InlineKeyboardMarkup::class.java, markup)
        val rows = inlineMarkup.inlineKeyboard()

        assertEquals(2, rows.size)
        assertEquals(3, rows[0].size)
        assertEquals(1, rows[1].size)
        assertEquals("m:one", rows[0][0].callbackData)
        assertEquals("m:three", rows[0][2].callbackData)
        assertEquals("m:four", rows[1][0].callbackData)
    }

    @Test
    fun `keyboard preserves mini app action position`() {
        val message =
            OutboundTelegramMessage(
                text = "menu",
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actions =
                    listOf(
                        OutboundTelegramMessageAction.Callback("Online", "m:online"),
                        OutboundTelegramMessageAction.Callback("Events", "m:events"),
                        OutboundTelegramMessageAction.OpenMiniApp("Open Mnemos"),
                    ),
            )

        val markup = message.toReplyMarkup(MINI_APP_URL)
        val inlineMarkup = assertInstanceOf(InlineKeyboardMarkup::class.java, markup)
        val rows = inlineMarkup.inlineKeyboard()

        assertEquals(2, rows.size)
        assertEquals("m:online", rows[0][0].callbackData)
        assertEquals("m:events", rows[0][1].callbackData)
        assertEquals("Open Mnemos", rows[1][0].text)
    }

    private companion object {
        const val MINI_APP_URL = "https://app.example.com"
    }
}
