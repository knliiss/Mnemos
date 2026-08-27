package com.mnemos.event.configuration.playeridentity

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.event.identity-resolution")
data class PlayerIdentityResolutionProperties(
    val maxAge: Duration = Duration.ofMinutes(30),
) {
    init {
        require(!maxAge.isNegative && !maxAge.isZero) {
            "Event identity resolution max age must be positive"
        }
    }
}
