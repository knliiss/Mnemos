package com.mnemos.telegram.bot.application.widget.query

data class TelegramWidgetQueryContext(
    val queryId: String,
    val chatId: Long,
    val actorId: Long,
    val messageId: Int,
    val data: String,
    val type: String,
    val isPrivate: Boolean,
    val isShared: Boolean,
)
