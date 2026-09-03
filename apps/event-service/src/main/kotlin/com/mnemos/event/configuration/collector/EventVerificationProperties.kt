package com.mnemos.event.configuration.collector

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.collector.verification")
data class EventVerificationProperties(
    val matchingWindow: Duration = Duration.ofSeconds(5),
    val crossCollectorDuplicateWindow: Duration = Duration.ofSeconds(30),
    val candidateTtl: Duration = Duration.ofMinutes(1),
) {
    init {
        require(!matchingWindow.isNegative)
        require(!matchingWindow.isZero)

        require(!crossCollectorDuplicateWindow.isNegative)
        require(!crossCollectorDuplicateWindow.isZero)
        require(crossCollectorDuplicateWindow >= matchingWindow)

        require(candidateTtl > crossCollectorDuplicateWindow)
    }
}
