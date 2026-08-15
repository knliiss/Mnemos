package com.mnemos.social.configuration

import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Clock

@EnableScheduling
@EnableConfigurationProperties(DropSpotlightProperties::class)
@Configuration(proxyBeanMethods = false)
class SocialConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
