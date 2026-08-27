package com.mnemos.event.configuration.playeridentity

import com.mnemos.event.application.playeridentity.policy.PlayerIdentityResolutionPolicy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PlayerIdentityResolutionProperties::class)
class PlayerIdentityResolutionConfiguration {
    @Bean
    fun playerIdentityResolutionPolicy(
        properties: PlayerIdentityResolutionProperties,
    ): PlayerIdentityResolutionPolicy =
        PlayerIdentityResolutionPolicy(
            maxAge = properties.maxAge,
        )
}
