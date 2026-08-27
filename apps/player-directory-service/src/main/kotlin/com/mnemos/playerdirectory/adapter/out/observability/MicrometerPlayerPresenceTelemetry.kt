package com.mnemos.playerdirectory.adapter.out.observability

import com.mnemos.playerdirectory.application.player.port.out.PlayerPresenceTelemetry
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MicrometerPlayerPresenceTelemetry(
    meterRegistry: MeterRegistry,
) : PlayerPresenceTelemetry {
    private val offlineDetectionLatency =
        Timer
            .builder("mnemos.player_directory.presence.offline_detection_latency")
            .description("Time from the first offline candidate observation to confirmed offline presence")
            .register(meterRegistry)

    override fun recordOfflineDetectionLatency(latency: Duration) {
        if (latency.isNegative) {
            return
        }

        offlineDetectionLatency.record(latency)
    }
}
