package com.mnemos.telegram.bot.configuration.widget

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.telegram-bot.widgets")
data class TelegramWidgetProperties(
    val enabled: Boolean = false,
)
