package com.mnemos.telegram.bot.configuration.telegram

import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import com.pengrad.telegrambot.TelegramBot
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableConfigurationProperties(TelegramBotProperties::class, TelegramWidgetProperties::class)
class TelegramConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
    fun telegramBot(properties: TelegramBotProperties): TelegramBot {
        val httpClient =
            OkHttpClient
                .Builder()
                .readTimeout(Duration.ofSeconds(60))
                .build()

        return TelegramBot
            .Builder(properties.token)
            .okHttpClient(httpClient)
            .build()
    }
}
