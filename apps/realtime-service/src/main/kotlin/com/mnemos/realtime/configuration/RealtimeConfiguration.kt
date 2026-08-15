package com.mnemos.realtime.configuration

import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.SecureRandom
import java.time.Clock

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    CollectorConnectionProperties::class,
    RealtimeDeliveryProperties::class,
)
class RealtimeConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun secureRandom(): SecureRandom = SecureRandom()
}
