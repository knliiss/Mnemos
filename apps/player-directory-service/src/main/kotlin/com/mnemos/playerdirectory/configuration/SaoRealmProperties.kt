package com.mnemos.playerdirectory.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.player-directory.realms")
data class SaoRealmProperties(
    val freshness: Duration = Duration.ofMinutes(1),
    val cacheTtl: Duration = Duration.ofSeconds(10),
) {
    init {
        require(!freshness.isNegative && !freshness.isZero) {
            "SAO realm freshness must be positive"
        }
        require(!cacheTtl.isNegative && !cacheTtl.isZero) {
            "SAO realm cache TTL must be positive"
        }
    }
}
