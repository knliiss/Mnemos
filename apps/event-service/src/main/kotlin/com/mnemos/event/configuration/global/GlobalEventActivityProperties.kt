package com.mnemos.event.configuration.global

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.event.global")
data class GlobalEventActivityProperties(
    val activeDuration: Duration = Duration.ofMinutes(15),
    val moonActiveDuration: Duration = Duration.ofMinutes(20),
    val eclipseActiveDuration: Duration = Duration.ofMinutes(30),
    val maximumSimultaneousEvents: Int = 3,
) {
    val maximumActiveDuration: Duration
        get() = maxOf(activeDuration, moonActiveDuration, eclipseActiveDuration)

    init {
        require(!activeDuration.isNegative)
        require(!activeDuration.isZero)
        require(!moonActiveDuration.isNegative)
        require(!moonActiveDuration.isZero)
        require(!eclipseActiveDuration.isNegative)
        require(!eclipseActiveDuration.isZero)
        require(maximumSimultaneousEvents > 0)
    }
}
