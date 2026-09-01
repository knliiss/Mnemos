package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage

object TelegramMessageLayout {
    const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

    fun normalize(message: OutboundTelegramMessage): OutboundTelegramMessage =
        message.copy(
            text = normalizeDivider(message.text),
        )

    private fun normalizeDivider(text: String): String =
        text
            .lineSequence()
            .joinToString("\n") { line ->
                if (line.isDividerLine()) {
                    DIVIDER
                } else {
                    line
                }
            }

    private fun String.isDividerLine(): Boolean = isNotEmpty() && all { character -> character == '━' }
}
