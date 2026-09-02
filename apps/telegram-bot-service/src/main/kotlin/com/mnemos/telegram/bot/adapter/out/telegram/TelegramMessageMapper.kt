package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.pengrad.telegrambot.model.WebAppInfo
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.ParseMode

object TelegramMessageMapper {
    fun OutboundTelegramMessageParseMode.toTelegram(): ParseMode? =
        when (this) {
            OutboundTelegramMessageParseMode.HTML -> ParseMode.HTML
            OutboundTelegramMessageParseMode.MARKDOWN_V2 -> ParseMode.MarkdownV2
            OutboundTelegramMessageParseMode.PLAIN -> null
        }

    fun OutboundTelegramMessage.toReplyMarkup(miniAppUrl: String): Keyboard? {
        val rows = buildRows(miniAppUrl)

        if (rows.isEmpty()) {
            return null
        }

        val markup = InlineKeyboardMarkup(*rows.first())

        rows.drop(1).forEach { row ->
            markup.addRow(*row)
        }

        return markup
    }

    private fun OutboundTelegramMessage.buildRows(miniAppUrl: String): List<Array<InlineKeyboardButton>> {
        if (actionRows.isNotEmpty()) {
            return actionRows.map { row ->
                row.map { action -> action.toTelegramButton(miniAppUrl) }.toTypedArray()
            }
        }

        return buildAdaptiveRows(actions, miniAppUrl)
    }

    private fun buildAdaptiveRows(
        actions: List<OutboundTelegramMessageAction>,
        miniAppUrl: String,
    ): List<Array<InlineKeyboardButton>> {
        val rows = mutableListOf<Array<InlineKeyboardButton>>()
        val callbackBuffer = mutableListOf<OutboundTelegramMessageAction.Callback>()

        fun flushCallbacks() {
            if (callbackBuffer.isEmpty()) {
                return
            }

            val buttonsPerRow = buttonsPerAdaptiveRow(callbackBuffer)

            callbackBuffer
                .chunked(buttonsPerRow)
                .forEach { row ->
                    rows += row.map { action -> action.toTelegramButton(miniAppUrl) }.toTypedArray()
                }

            callbackBuffer.clear()
        }

        actions.forEach { action ->
            when (action) {
                is OutboundTelegramMessageAction.Callback -> callbackBuffer += action
                is OutboundTelegramMessageAction.OpenMiniApp,
                is OutboundTelegramMessageAction.OpenUrl,
                -> {
                    flushCallbacks()
                    rows += arrayOf(action.toTelegramButton(miniAppUrl))
                }
            }
        }

        flushCallbacks()

        return rows
    }

    private fun buttonsPerAdaptiveRow(actions: List<OutboundTelegramMessageAction.Callback>): Int {
        val longestLabel = actions.maxOfOrNull { action -> action.label.length } ?: return 1

        return when {
            longestLabel <= 5 -> 5
            longestLabel <= 11 -> 4
            longestLabel <= 18 -> 3
            longestLabel <= 28 -> 2
            else -> 1
        }
    }

    private fun OutboundTelegramMessageAction.toTelegramButton(miniAppUrl: String): InlineKeyboardButton {
        val button =
            when (this) {
                is OutboundTelegramMessageAction.OpenMiniApp ->
                    InlineKeyboardButton(label).webApp(WebAppInfo(miniAppUrl))

                is OutboundTelegramMessageAction.OpenUrl ->
                    InlineKeyboardButton(label).url(url)

                is OutboundTelegramMessageAction.Callback ->
                    InlineKeyboardButton(label).callbackData(data)
            }

        resolvedStyle().telegramValue?.let(button::style)

        return button
    }

    private fun OutboundTelegramMessageAction.resolvedStyle(): OutboundTelegramButtonStyle {
        if (style != OutboundTelegramButtonStyle.DEFAULT) {
            return style
        }

        return when {
            label.startsWith("✖") || label.startsWith("✕") || label.startsWith("🗑") ->
                OutboundTelegramButtonStyle.DANGER

            label.startsWith("✅") || label.startsWith("✓") || label.startsWith("🎁") || label.startsWith("⭐") ->
                OutboundTelegramButtonStyle.SUCCESS

            label.startsWith("➕") || label.startsWith("🔎") ->
                OutboundTelegramButtonStyle.PRIMARY

            else -> OutboundTelegramButtonStyle.DEFAULT
        }
    }
}
