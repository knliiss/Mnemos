package com.mnemos.playerdirectory.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.player-directory.tracking")
data class PlayerTrackingProperties(
    val batchSize: Int,
    val leaseDuration: Duration,
    val subscriptionPollInterval: Duration,
    val saoOnlinePollInterval: Duration,
    val highActivityPollInterval: Duration,
    val lowActivityPollInterval: Duration,
    val unverifiablePollInterval: Duration,
    val lowActivityDropThreshold: Long,
    val lowActivityRetention: Duration,
    val highActivityRetention: Duration,
    val failureBackoff: Duration,
    val maximumFailureBackoff: Duration,
    val linkedAccountPollInterval: Duration = Duration.ofHours(6),
) {
    init {
        require(batchSize in 1..200) {
            "Player tracking batch size must be between 1 and 200"
        }
        requirePositive(leaseDuration, "Player tracking lease duration")
        requirePositive(subscriptionPollInterval, "Subscription poll interval")
        requirePositive(saoOnlinePollInterval, "SAO online poll interval")
        requirePositive(highActivityPollInterval, "High-activity poll interval")
        requirePositive(lowActivityPollInterval, "Low-activity poll interval")
        requirePositive(unverifiablePollInterval, "Unverifiable presence poll interval")
        require(lowActivityDropThreshold >= 0) {
            "Low-activity drop threshold cannot be negative"
        }
        requirePositive(lowActivityRetention, "Low-activity retention")
        require(highActivityRetention >= lowActivityRetention) {
            "High-activity retention cannot be shorter than low-activity retention"
        }
        requirePositive(linkedAccountPollInterval, "Linked account nickname refresh interval")
        requirePositive(failureBackoff, "Tracking failure backoff")
        require(maximumFailureBackoff >= failureBackoff) {
            "Maximum tracking failure backoff cannot be smaller than the base backoff"
        }
    }

    private fun requirePositive(
        duration: Duration,
        name: String,
    ) {
        require(!duration.isNegative && !duration.isZero) {
            "$name must be positive"
        }
    }
}
