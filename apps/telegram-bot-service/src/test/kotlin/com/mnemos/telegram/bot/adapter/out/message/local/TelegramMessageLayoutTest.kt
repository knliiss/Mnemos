package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
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

    @Test
    fun `normalization marks selected numeric toggle labels explicitly`() {
        val selected =
            OutboundTelegramMessageAction.Callback(
                label = "12",
                data = "ntf:rl:12:0",
                style = OutboundTelegramButtonStyle.SUCCESS,
            )
        val inactive =
            OutboundTelegramMessageAction.Callback(
                label = "13",
                data = "ntf:rl:13:1",
            )
        val message =
            OutboundTelegramMessage(
                text = "RAID",
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actions = listOf(selected, inactive),
                actionRows = listOf(listOf(selected, inactive)),
            )

        val normalized = TelegramMessageLayout.normalize(message)
        val callbacks = normalized.actionRows.single().filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertEquals("✓ 12", callbacks[0].label)
        assertEquals("13", callbacks[1].label)
    }
}
