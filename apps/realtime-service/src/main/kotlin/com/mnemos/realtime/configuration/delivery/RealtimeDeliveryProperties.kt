package com.mnemos.realtime.configuration.delivery

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.realtime.delivery")
data class RealtimeDeliveryProperties(
    val timeout: Duration = Duration.ofMinutes(30),
    val heartbeatInterval: Duration = Duration.ofSeconds(15),
    val replayTtl: Duration = Duration.ofHours(24),
    val replayLimit: Long = 100,
    val deduplicationLimit: Int = 512,
    val streamPollTimeout: Duration = Duration.ofMillis(250),
    val streamBatchSize: Int = 100,
    val streamMaxLength: Long = 100_000,
    val maxConnectionsPerUser: Int = 3,
    val maxConnectionsPerClientIp: Int = 20,
    val maxConnectionsTotal: Int = 5_000,
    val connectionLeaseTtl: Duration = Duration.ofSeconds(45),
) {
    init {
        require(!timeout.isNegative && !timeout.isZero)
        require(!heartbeatInterval.isNegative && !heartbeatInterval.isZero)
        require(!replayTtl.isNegative && !replayTtl.isZero)
        require(replayLimit > 0)
        require(deduplicationLimit >= replayLimit)
        require(!streamPollTimeout.isNegative && !streamPollTimeout.isZero)
        require(streamBatchSize > 0)
        require(streamMaxLength >= replayLimit)
        require(maxConnectionsPerUser > 0)
        require(maxConnectionsPerClientIp > 0)
        require(maxConnectionsTotal > 0)
        require(maxConnectionsPerUser <= maxConnectionsPerClientIp)
        require(maxConnectionsPerClientIp <= maxConnectionsTotal)
        require(connectionLeaseTtl >= heartbeatInterval.multipliedBy(2)) {
            "Realtime connection lease TTL must cover at least two heartbeat intervals"
        }
    }
}
