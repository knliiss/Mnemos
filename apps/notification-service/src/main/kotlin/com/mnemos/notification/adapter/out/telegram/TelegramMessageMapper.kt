package com.mnemos.notification.adapter.out.telegram

import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.MessageParseMode
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.pengrad.telegrambot.model.WebAppInfo
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup

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

        val buttons =
            actions
                .map { action ->
                    when (action) {
                        is MessageAction.OpenMiniApp ->
                            KeyboardButton(action.label)
                                .webAppInfo(WebAppInfo(miniAppUrl))
                    }
                }.toTypedArray()

        return ReplyKeyboardMarkup(*buttons)
            .resizeKeyboard(true)
    }
}
