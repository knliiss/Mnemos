package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction

object TelegramMessageLayout {
    const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

    fun normalize(message: OutboundTelegramMessage): OutboundTelegramMessage =
        message.copy(
            text = normalizeDivider(message.text),
            actions = message.actions.map(OutboundTelegramMessageAction::withSelectedCompactLabel),
            actionRows =
                message.actionRows.map { row ->
                    row.map(OutboundTelegramMessageAction::withSelectedCompactLabel)
                },
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

    private fun OutboundTelegramMessageAction.withSelectedCompactLabel(): OutboundTelegramMessageAction {
        if (this !is OutboundTelegramMessageAction.Callback) {
            return this
        }

        if (style != OutboundTelegramButtonStyle.SUCCESS || !label.isCompactNumber()) {
            return this
        }

        return copy(label = "✓ $label")
    }

    private fun String.isCompactNumber(): Boolean = length in 1..2 && all(Char::isDigit)

    private fun String.isDividerLine(): Boolean = isNotEmpty() && all { character -> character == '━' }
}
