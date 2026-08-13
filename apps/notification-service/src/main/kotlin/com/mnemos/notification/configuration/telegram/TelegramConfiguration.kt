package com.mnemos.notification.configuration.telegram

import com.pengrad.telegrambot.TelegramBot
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableConfigurationProperties(TelegramBotProperties::class)
class TelegramConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
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
