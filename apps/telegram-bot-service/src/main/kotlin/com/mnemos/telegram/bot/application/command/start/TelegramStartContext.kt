package com.mnemos.telegram.bot.application.command.start

data class TelegramStartContext(
    val telegramId: Long,
    val firstName: String? = null,
    val username: String? = null,
    val parameter: String? = null,
)
