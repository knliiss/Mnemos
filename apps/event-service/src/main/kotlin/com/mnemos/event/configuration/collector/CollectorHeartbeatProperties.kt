package com.mnemos.event.configuration.collector

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.collector.heartbeat")
data class CollectorHeartbeatProperties(
    val interval: Duration = Duration.ofSeconds(10),
    val timeout: Duration = Duration.ofSeconds(30),
) {
    init {
        require(!interval.isZero)
        require(!interval.isNegative)

        require(timeout > interval)
    }
}
