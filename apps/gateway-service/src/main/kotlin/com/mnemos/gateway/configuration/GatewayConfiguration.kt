package com.mnemos.gateway.configuration

import com.mnemos.gateway.ratelimit.GatewayRateLimitProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayRateLimitProperties::class, ModerationClientProperties::class)
class GatewayConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
