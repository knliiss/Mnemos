package com.mnemos.realtime.configuration.collector

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize
import java.time.Duration

@ConfigurationProperties("mnemos.realtime.collector")
data class CollectorConnectionProperties(
    val maxMessageSize: DataSize = DataSize.ofKilobytes(64),
    val messagesPerSecond: Int = 20,
    val maxInFlightMessages: Int = 100,
    val kafkaTimeout: Duration = Duration.ofSeconds(2),
    val heartbeatInterval: Duration = Duration.ofSeconds(10),
    val heartbeatTimeout: Duration = Duration.ofSeconds(30),
    val observationTimeout: Duration = Duration.ofMinutes(10),
    val authorizationFailureGrace: Duration = Duration.ofSeconds(30),
    val activationTtl: Duration = Duration.ofMinutes(10),
    val updateSlotTtl: Duration = Duration.ofMinutes(2),
    val updateRetryDelay: Duration = Duration.ofSeconds(30),
) {
    init {
        require(maxMessageSize.toBytes() > 0)
        require(maxMessageSize.toBytes() <= Int.MAX_VALUE)
        require(messagesPerSecond > 0)
        require(maxInFlightMessages > 0)
        require(!kafkaTimeout.isNegative && !kafkaTimeout.isZero)
        require(!heartbeatInterval.isNegative && !heartbeatInterval.isZero)
        require(heartbeatTimeout > heartbeatInterval)
        require(observationTimeout > heartbeatInterval)
        require(authorizationFailureGrace >= heartbeatInterval)
        require(!activationTtl.isNegative && !activationTtl.isZero)
        require(!updateSlotTtl.isNegative && !updateSlotTtl.isZero)
        require(!updateRetryDelay.isNegative && !updateRetryDelay.isZero)
    }
}
