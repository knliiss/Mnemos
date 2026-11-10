package com.mnemos.telegram.bot.application.widget.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Base64
import kotlin.random.Random

class TelegramWidgetMessageTest {
    @Test
    fun acceptsSmallSelfContainedWidgetAndShortCallbackData() {
        val message =
            TelegramWidgetMessage(
                html = "<b>Mnemos</b>",
                caption = "Головна панель",
                actionRows = listOf(listOf(TelegramWidgetButton("Події", "w:open:events"))),
            )

        assertEquals(1, message.actionRows.size)
    }

    @Test
    fun rejectsEmptyOrOversizedWidget() {
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(html = "", caption = "caption")
        }
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(html = "x".repeat(32_769), caption = "caption")
        }
    }

    @Test
    fun enforcesCompressedHtmlSizeAndCountsBytes() {
        val randomText = Base64.getEncoder().encodeToString(Random(7).nextBytes(9_000))

        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(html = randomText, caption = "caption")
        }
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetButton(label = "action", callbackData = "ї".repeat(33))
        }
    }

    @Test
    fun rejectsNulInHtmlAndChatDataBeforeSendingToTelegram() {
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(html = "<div>\\u0000</div>".replace("\\u0000", "\u0000"), caption = "invalid")
        }
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(html = "<div>Good</div>", caption = "invalid", chatData = "\u0000")
        }
    }

    @Test
    fun rejectsEmptyKeyboardRowsAndCaptions() {
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(html = "<b>Test</b>", caption = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            TelegramWidgetMessage(
                html = "<b>Test</b>",
                caption = "caption",
                actionRows = listOf(emptyList()),
            )
        }
    }
}
