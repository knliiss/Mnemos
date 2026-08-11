package com.mnemos.notification.configuration.telegram

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.notification.telegram")
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
