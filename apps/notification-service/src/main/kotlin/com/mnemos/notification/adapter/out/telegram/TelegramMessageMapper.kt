package com.mnemos.notification.adapter.out.telegram

import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.MessageParseMode
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.pengrad.telegrambot.model.WebAppInfo
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.ParseMode

object TelegramMessageMapper {
    fun MessageParseMode.toTelegram(): ParseMode? =
        when (this) {
            MessageParseMode.HTML -> ParseMode.HTML
            MessageParseMode.MARKDOWN_V2 -> ParseMode.MarkdownV2
            MessageParseMode.PLAIN -> null
        }

    fun RenderedMessage.toReplyMarkup(miniAppUrl: String): Keyboard? {
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

    private fun RenderedMessage.buildRows(miniAppUrl: String): List<Array<InlineKeyboardButton>> {
        val miniAppRows =
            actions
                .filterIsInstance<MessageAction.OpenMiniApp>()
                .map { action ->
                    arrayOf(
                        InlineKeyboardButton(action.label)
                            .webApp(WebAppInfo(miniAppUrl)),
                    )
                }
        val callbackRows =
            actions
                .filterIsInstance<MessageAction.Callback>()
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
