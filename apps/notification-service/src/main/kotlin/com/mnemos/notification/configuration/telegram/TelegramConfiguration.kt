package com.mnemos.notification.configuration.telegram

import com.pengrad.telegrambot.TelegramBot
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TelegramBotProperties::class)
class TelegramConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
    fun telegramBot(properties: TelegramBotProperties): TelegramBot = TelegramBot(properties.token)
}
