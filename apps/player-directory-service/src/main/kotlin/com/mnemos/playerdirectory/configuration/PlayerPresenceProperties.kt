package com.mnemos.playerdirectory.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mnemos.player-directory.presence")
data class PlayerPresenceProperties(
    val offlineConfirmationDelay: Duration,
    val requiredOfflineObservations: Int,
) {
    init {
        require(!offlineConfirmationDelay.isNegative) {
            "Offline confirmation delay cannot be negative"
        }
        require(requiredOfflineObservations >= 2) {
            "At least two offline observations are required"
        }
    }
}
