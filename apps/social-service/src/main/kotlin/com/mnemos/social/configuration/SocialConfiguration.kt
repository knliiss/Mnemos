package com.mnemos.social.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
class SocialConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
