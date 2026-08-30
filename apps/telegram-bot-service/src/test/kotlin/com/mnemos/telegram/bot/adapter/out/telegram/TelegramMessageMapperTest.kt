package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.adapter.out.telegram.TelegramMessageMapper.toReplyMarkup
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class TelegramMessageMapperTest {
    @Test
    fun `mini app action uses inline web app button`() {
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
    }

    @Test
    fun `callback actions are grouped into compact rows`() {
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

        assertEquals(3, rows.size)
        assertEquals(1, rows[0].size)
        assertEquals(2, rows[1].size)
        assertEquals(1, rows[2].size)
        assertEquals("help:overview", rows[1][0].callbackData)
        assertEquals("help:accounts", rows[1][1].callbackData)
    }

    private companion object {
        const val MINI_APP_URL = "https://app.example.com"
    }
}
