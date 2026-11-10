package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage

object TelegramWidgetRequestMapper {
    fun toSendBody(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): Map<String, Any> =
        buildMap {
            put("chat_id", telegramId)
            put("html", widget.html)
            put("caption", widget.caption)
            put("allow_queries", widget.allowQueries)
            widget.chatData?.let { put("chat_data", it) }

            if (widget.actionRows.isNotEmpty()) {
                put(
                    "reply_markup",
                    mapOf(
                        "inline_keyboard" to widget.actionRows.map { row ->
                            row.map { button ->
                                mapOf(
                                    "text" to button.label,
                                    "callback_data" to button.callbackData,
                                )
                            }
                        },
                    ),
                )
            }
        }
