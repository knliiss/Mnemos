package com.mnemos.playerdirectory.configuration

import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingExecutionPolicy
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingSchedulePolicy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableConfigurationProperties(PlayerTrackingProperties::class)
class PlayerDirectoryConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun playerTrackingExecutionPolicy(properties: PlayerTrackingProperties): PlayerTrackingExecutionPolicy =
        PlayerTrackingExecutionPolicy(
            batchSize = properties.batchSize,
            leaseDuration = properties.leaseDuration,
        )

    @Bean
    fun playerTrackingSchedulePolicy(properties: PlayerTrackingProperties): PlayerTrackingSchedulePolicy =
        PlayerTrackingSchedulePolicy(
            subscriptionPollInterval = properties.subscriptionPollInterval,
            unverifiablePollInterval = properties.unverifiablePollInterval,
            failureBackoff = properties.failureBackoff,
            maximumFailureBackoff = properties.maximumFailureBackoff,
        )
}
