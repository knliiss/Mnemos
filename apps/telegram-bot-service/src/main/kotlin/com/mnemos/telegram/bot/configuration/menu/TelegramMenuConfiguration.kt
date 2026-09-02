package com.mnemos.telegram.bot.configuration.menu

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TelegramMenuProperties::class)
class TelegramMenuConfiguration
