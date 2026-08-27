package com.mnemos.playerdirectory.adapter.out.observability

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeUnit

class MicrometerPlayerPresenceTelemetryTest {
    @Test
    fun `offline detection latency is recorded as timer duration`() {
        val registry = SimpleMeterRegistry()
        val telemetry = MicrometerPlayerPresenceTelemetry(registry)

        telemetry.recordOfflineDetectionLatency(Duration.ofSeconds(12))
        telemetry.recordOfflineDetectionLatency(Duration.ofSeconds(-1))

        val timer = registry.get("mnemos.player_directory.presence.offline_detection_latency").timer()

        assertEquals(1, timer.count())
        assertEquals(12.0, timer.totalTime(TimeUnit.SECONDS))
    }
}
