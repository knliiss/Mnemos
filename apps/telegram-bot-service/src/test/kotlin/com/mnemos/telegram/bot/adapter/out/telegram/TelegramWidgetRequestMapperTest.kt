package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class TelegramWidgetRequestMapperTest {
    @Test
    fun mapsWidgetToTelegramBotApi104PayloadWithoutEnablingUnsupportedQueries() {
        val document =
            TelegramWidgetMessage(
                html = "<h3>Mnemos</h3>",
                caption = "Mnemos dashboard",
                actionRows = listOf(listOf(TelegramWidgetButton("Events", "w:open:events"))),
            )

        val body = TelegramWidgetRequestMapper.toSendBody(123L, document)

        assertEquals(123L, body["chat_id"])
        assertEquals(document.html, body["html"])
        assertEquals(document.caption, body["caption"])
        assertFalse(body["allow_queries"] as Boolean)
        val markup = body["reply_markup"] as Map<*, *>
        val rows = markup["inline_keyboard"] as List<*>
        val buttons = rows.first() as List<*>
        val button = buttons.first() as Map<*, *>
        assertEquals("Events", button["text"])
        assertEquals("w:open:events", button["callback_data"])
    }
}
