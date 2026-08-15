package com.mnemos.personalization.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class PersonalizationConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
