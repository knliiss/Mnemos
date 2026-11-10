package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TelegramWidgetRequestMapperTest {
    @Test
    fun mapsInteractiveWidgetWithInitialChatDataAndEnabledQueries() {
        val document = TelegramWidgetMessage(
            html = "<h3>Mnemos</h3>",
            caption = "Mnemos dashboard",
            actionRows = listOf(listOf(TelegramWidgetButton("Events", "w:open:events"))),
            chatData = """{"page":0}""",
            allowQueries = true,
        )

        val body = TelegramWidgetRequestMapper.toSendBody(123L, document)

        assertEquals(123L, body["chat_id"])
        assertEquals(document.html, body["html"])
        assertEquals(document.caption, body["caption"])
        assertTrue(body["allow_queries"] as Boolean)
        assertEquals("""{"page":0}""", body["chat_data"])
        val markup = body["reply_markup"] as Map<*, *>
        val rows = markup["inline_keyboard"] as List<*>
        val buttons = rows.first() as List<*>
        val button = buttons.first() as Map<*, *>
        assertEquals("Events", button["text"])
        assertEquals("w:open:events", button["callback_data"])
    }

    @Test
    fun keepsReadOnlyWidgetNonInteractiveWhileSendingItsInitialData() {
        val document = TelegramWidgetMessage(
            html = "<strong>Readonly</strong>",
            caption = "Details",
            chatData = """{"type":"ITEM_DROP"}""",
            allowQueries = false,
        )

        val body = TelegramWidgetRequestMapper.toSendBody(123L, document)

        assertFalse(body["allow_queries"] as Boolean)
        assertEquals("""{"type":"ITEM_DROP"}""", body["chat_data"])
        assertNull(body["reply_markup"])
    }

    @Test
    fun omitsUnspecifiedInitialState() {
        val body = TelegramWidgetRequestMapper.toSendBody(
            123L,
            TelegramWidgetMessage(html = "<b>Hi</b>", caption = "Static"),
        )

        assertFalse(body.containsKey("chat_data"))
        assertFalse(body["allow_queries"] as Boolean)
    }
}
