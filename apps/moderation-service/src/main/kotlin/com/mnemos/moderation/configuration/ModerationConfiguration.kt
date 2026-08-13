package com.mnemos.moderation.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
class ModerationConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
