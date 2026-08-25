package com.mnemos.event.configuration.global

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.event.global")
data class GlobalEventActivityProperties(
    val activeDuration: Duration = Duration.ofMinutes(15),
    val maximumSimultaneousEvents: Int = 3,
) {
    init {
        require(!activeDuration.isNegative)
        require(!activeDuration.isZero)
        require(maximumSimultaneousEvents > 0)
    }
}
