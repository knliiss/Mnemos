package com.mnemos.telegram.bot.configuration.telegram

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.telegram-bot.telegram")
data class TelegramBotProperties(
    val enabled: Boolean = false,
    val token: String = "",
    val miniAppUrl: String = "",
) {
    init {
        if (enabled) {
            require(token.isNotBlank())
            require(miniAppUrl.startsWith("https://"))
        }
    }
}
