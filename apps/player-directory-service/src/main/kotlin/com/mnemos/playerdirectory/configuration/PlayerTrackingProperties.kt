package com.mnemos.playerdirectory.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.player-directory.tracking")
data class PlayerTrackingProperties(
    val batchSize: Int,
    val leaseDuration: Duration,
    val subscriptionPollInterval: Duration,
    val unverifiablePollInterval: Duration,
    val failureBackoff: Duration,
    val maximumFailureBackoff: Duration,
) {
    init {
        require(batchSize in 1..200) { "Player tracking batch size must be between 1 and 200" }
        require(!leaseDuration.isNegative && !leaseDuration.isZero) { "Player tracking lease duration must be positive" }
        require(!subscriptionPollInterval.isNegative && !subscriptionPollInterval.isZero) {
            "Subscription poll interval must be positive"
        }
        require(!unverifiablePollInterval.isNegative && !unverifiablePollInterval.isZero) {
            "Unverifiable presence poll interval must be positive"
        }
        require(!failureBackoff.isNegative && !failureBackoff.isZero) { "Tracking failure backoff must be positive" }
        require(maximumFailureBackoff >= failureBackoff) {
            "Maximum tracking failure backoff cannot be smaller than the base backoff"
        }
    }
}
