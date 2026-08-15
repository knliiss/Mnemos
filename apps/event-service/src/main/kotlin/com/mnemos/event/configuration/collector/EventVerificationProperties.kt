package com.mnemos.event.configuration.collector

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.collector.verification")
data class EventVerificationProperties(
    val matchingWindow: Duration = Duration.ofSeconds(5),
    val candidateTtl: Duration = Duration.ofMinutes(1),
    val confirmationRatio: Double = 0.5,
    val minimumConfirmations: Int = 2,
) {
    init {
        require(!matchingWindow.isNegative)
        require(!matchingWindow.isZero)

        require(candidateTtl > matchingWindow)

        require(confirmationRatio > 0.0)
        require(confirmationRatio <= 1.0)

        require(minimumConfirmations > 0)
    }
}
