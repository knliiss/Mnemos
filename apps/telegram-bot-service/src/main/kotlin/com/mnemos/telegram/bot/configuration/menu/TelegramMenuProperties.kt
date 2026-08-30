package com.mnemos.telegram.bot.configuration.menu

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.telegram-bot.menu")
data class TelegramMenuProperties(
    val sessionTtl: Duration = Duration.ofDays(30),
) {
    init {
        require(!sessionTtl.isZero && !sessionTtl.isNegative)
    }
}
