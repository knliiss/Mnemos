package com.mnemos.playerdirectory.configuration

import com.mnemos.playerdirectory.application.activity.policy.PendingPlayerResolutionPolicy
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingActivityPolicy
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingExecutionPolicy
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingSchedulePolicy
import com.mnemos.playerdirectory.domain.player.policy.PlayerPresenceTransitionPolicy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableConfigurationProperties(
    PlayerPresenceProperties::class,
    PlayerResolutionProperties::class,
    PlayerTrackingProperties::class,
)
class PlayerDirectoryConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun playerPresenceTransitionPolicy(properties: PlayerPresenceProperties): PlayerPresenceTransitionPolicy =
        PlayerPresenceTransitionPolicy(
            offlineConfirmationDelay = properties.offlineConfirmationDelay,
            requiredOfflineObservations = properties.requiredOfflineObservations,
        )

    @Bean
    fun pendingPlayerResolutionPolicy(properties: PlayerResolutionProperties): PendingPlayerResolutionPolicy =
        PendingPlayerResolutionPolicy(
            batchSize = properties.batchSize,
            leaseDuration = properties.leaseDuration,
            retryBackoff = properties.retryBackoff,
            maximumRetryBackoff = properties.maximumRetryBackoff,
        )

    @Bean
    fun playerTrackingExecutionPolicy(properties: PlayerTrackingProperties): PlayerTrackingExecutionPolicy =
        PlayerTrackingExecutionPolicy(
            batchSize = properties.batchSize,
            leaseDuration = properties.leaseDuration,
        )

    @Bean
    fun playerTrackingActivityPolicy(properties: PlayerTrackingProperties): PlayerTrackingActivityPolicy =
        PlayerTrackingActivityPolicy(
            lowActivityDropThreshold = properties.lowActivityDropThreshold,
            lowActivityRetention = properties.lowActivityRetention,
            highActivityRetention = properties.highActivityRetention,
        )

    @Bean
    fun playerTrackingSchedulePolicy(properties: PlayerTrackingProperties): PlayerTrackingSchedulePolicy =
        PlayerTrackingSchedulePolicy(
            subscriptionPollInterval = properties.subscriptionPollInterval,
            saoOnlinePollInterval = properties.saoOnlinePollInterval,
            highActivityPollInterval = properties.highActivityPollInterval,
            lowActivityPollInterval = properties.lowActivityPollInterval,
            unverifiablePollInterval = properties.unverifiablePollInterval,
            failureBackoff = properties.failureBackoff,
            maximumFailureBackoff = properties.maximumFailureBackoff,
            linkedAccountPollInterval = properties.linkedAccountPollInterval,
        )
}
