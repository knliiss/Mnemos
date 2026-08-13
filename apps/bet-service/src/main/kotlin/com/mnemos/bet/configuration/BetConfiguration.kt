package com.mnemos.bet.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    EventBetProperties::class,
    ItemRaceProperties::class,
)
class BetConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
