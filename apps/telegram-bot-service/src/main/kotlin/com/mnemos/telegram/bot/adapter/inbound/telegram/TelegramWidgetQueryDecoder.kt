package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import tools.jackson.databind.JsonNode

object TelegramWidgetQueryDecoder {
    fun parse(query: JsonNode): TelegramWidgetQueryContext? {
        if (!query.isObject) return null

        val queryId = query.path("id").asText("")
        val chat = query.path("chat")
        val chatId = chat.path("id").asLong(0L)
        val actorId = query.path("from").path("id").asLong(0L)
        val messageId = query.path("message_id").asInt(0)
        if (queryId.isBlank() || chatId <= 0 || actorId <= 0 || messageId <= 0) return null

        return TelegramWidgetQueryContext(
            queryId = queryId,
            chatId = chatId,
            actorId = actorId,
            messageId = messageId,
            data = query.path("data").asText(""),
            type = query.path("type").path("type").asText(""),
            isPrivate = chat.path("type").asText("") == "private",
            isShared = query.hasNonNull("widget_id") || query.hasNonNull("inline_message_id"),
        )
    }
}
