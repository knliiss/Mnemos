package com.mnemos.telegram.bot.application.command

data class TelegramCommandContext(
    val telegramId: Long,
    val text: String,
    val firstName: String?,
    val username: String?,
)
