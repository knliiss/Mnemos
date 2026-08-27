package com.mnemos.playerdirectory.adapter.out.observability

import com.mnemos.playerdirectory.application.observability.model.PlayerDirectoryOperationalSnapshot
import com.mnemos.playerdirectory.application.observability.port.out.PlayerDirectoryOperationalSnapshotReader
import com.mnemos.playerdirectory.configuration.PlayerDirectoryObservabilityProperties
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class PlayerDirectoryOperationalMetricsTest {
    @Test
    fun `refresh publishes operational snapshot gauges`() {
        val now = Instant.parse("2026-08-27T12:00:00Z")
        val stalePresenceAfter = Duration.ofMinutes(5)
        val reader =
            PlayerDirectoryOperationalSnapshotReader { readAt, staleBefore ->
                assertEquals(now, readAt)
                assertEquals(now.minus(stalePresenceAfter), staleBefore)

                PlayerDirectoryOperationalSnapshot(
                    trackingDueCount = 7,
                    oldestTrackingDueAt = now.minusSeconds(42),
                    resolutionQueueCount = 3,
                    trackedOnlineCount = 11,
                    trackedSaoCount = 8,
                    stalePresenceCount = 2,
                )
            }
        val registry = SimpleMeterRegistry()
        val metrics =
            PlayerDirectoryOperationalMetrics(
                snapshotReader = reader,
                properties =
                    PlayerDirectoryObservabilityProperties(
                        refreshInterval = Duration.ofSeconds(15),
                        stalePresenceAfter = stalePresenceAfter,
                    ),
                clock = Clock.fixed(now, ZoneOffset.UTC),
                meterRegistry = registry,
            )

        metrics.refresh()

        assertEquals(7.0, registry.get("mnemos.player_directory.tracking.due").gauge().value())
        assertEquals(42.0, registry.get("mnemos.player_directory.tracking.oldest_overdue").gauge().value())
        assertEquals(3.0, registry.get("mnemos.player_directory.resolution.queue").gauge().value())
        assertEquals(11.0, registry.get("mnemos.player_directory.tracking.online").gauge().value())
        assertEquals(8.0, registry.get("mnemos.player_directory.tracking.sao").gauge().value())
        assertEquals(2.0, registry.get("mnemos.player_directory.presence.stale").gauge().value())
    }
}
