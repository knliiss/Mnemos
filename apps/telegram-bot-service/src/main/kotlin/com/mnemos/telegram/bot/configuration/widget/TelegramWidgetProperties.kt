package com.mnemos.telegram.bot.configuration.widget

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.telegram-bot.widgets")
data class TelegramWidgetProperties(
    val enabled: Boolean = false,
    val sessionTtl: Duration = Duration.ofDays(7),
) {
    init {
        require(!sessionTtl.isNegative && !sessionTtl.isZero)
    }
}
