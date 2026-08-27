package com.mnemos.playerdirectory.configuration

import com.mnemos.playerdirectory.application.realm.policy.SaoRealmReadPolicy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SaoRealmProperties::class)
class SaoRealmConfiguration {
    @Bean
    fun saoRealmReadPolicy(properties: SaoRealmProperties): SaoRealmReadPolicy =
        SaoRealmReadPolicy(
            freshness = properties.freshness,
            cacheTtl = properties.cacheTtl,
        )
}
