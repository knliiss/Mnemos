package com.mnemos.telegram.bot.adapter.out.telegram

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
        if (actions.isEmpty()) {
            return null
        }

        val rows = buildRows(miniAppUrl)
        val markup = InlineKeyboardMarkup(*rows.first())

        rows.drop(1).forEach { row ->
            markup.addRow(*row)
        }

        return markup
    }

    private fun OutboundTelegramMessage.buildRows(miniAppUrl: String): List<Array<InlineKeyboardButton>> {
        val miniAppRows =
            actions
                .filterIsInstance<OutboundTelegramMessageAction.OpenMiniApp>()
                .map { action ->
                    arrayOf(
                        InlineKeyboardButton(action.label)
                            .webApp(WebAppInfo(miniAppUrl)),
                    )
                }
        val callbackRows =
            actions
                .filterIsInstance<OutboundTelegramMessageAction.Callback>()
                .chunked(CALLBACK_BUTTONS_PER_ROW)
                .map { actions ->
                    actions
                        .map { action ->
                            InlineKeyboardButton(action.label)
                                .callbackData(action.data)
                        }.toTypedArray()
                }

        return miniAppRows + callbackRows
    }

    private const val CALLBACK_BUTTONS_PER_ROW = 2
}
