package com.mnemos.playerdirectory.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.player-directory.observability")
data class PlayerDirectoryObservabilityProperties(
    val refreshInterval: Duration = Duration.ofSeconds(15),
    val stalePresenceAfter: Duration = Duration.ofMinutes(5),
) {
    init {
        requirePositive(refreshInterval, "Observability refresh interval")
        requirePositive(stalePresenceAfter, "Stale presence threshold")
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
