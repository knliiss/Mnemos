package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage

object TelegramWidgetRequestMapper {
    fun toSendBody(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): Map<String, Any> =
        mapOf(
            "chat_id" to telegramId,
            "html" to widget.html,
            "caption" to widget.caption,
            "allow_queries" to false,
            "reply_markup" to
                mapOf(
                    "inline_keyboard" to
                        widget.actionRows.map { row ->
                            row.map { button ->
                                mapOf(
                                    "text" to button.label,
                                    "callback_data" to button.callbackData,
                                )
                            }
                        },
                ),
        )
