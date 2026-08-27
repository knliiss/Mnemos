package com.mnemos.playerdirectory.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.player-directory.resolution")
data class PlayerResolutionProperties(
    val batchSize: Int,
    val leaseDuration: Duration,
    val retryBackoff: Duration,
    val maximumRetryBackoff: Duration,
) {
    init {
        require(batchSize in 1..200) {
            "Pending player resolution batch size must be between 1 and 200"
        }
        require(!leaseDuration.isNegative && !leaseDuration.isZero) {
            "Pending player resolution lease duration must be positive"
        }
        require(!retryBackoff.isNegative && !retryBackoff.isZero) {
            "Pending player resolution retry backoff must be positive"
        }
        require(maximumRetryBackoff >= retryBackoff) {
            "Maximum pending player resolution retry backoff cannot be smaller than the base backoff"
        }
    }
}
