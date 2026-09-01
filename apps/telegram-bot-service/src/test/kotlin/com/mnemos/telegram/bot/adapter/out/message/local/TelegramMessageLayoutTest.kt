package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramMessageLayoutTest {
    @Test
    fun `normalization replaces divider lines with standard width`() {
        val message =
            OutboundTelegramMessage(
                text =
                    listOf(
                        "🤖 <b>Mnemos</b>",
                        "━━━━━━",
                        "content",
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    ).joinToString("\n"),
                parseMode = OutboundTelegramMessageParseMode.HTML,
            )

        val normalized = TelegramMessageLayout.normalize(message)
        val lines = normalized.text.lines()

        assertEquals(TelegramMessageLayout.DIVIDER, lines[1])
        assertEquals(TelegramMessageLayout.DIVIDER, lines[3])
    }
}
