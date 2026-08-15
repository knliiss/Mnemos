package com.mnemos.event.adapter.out.memory.collector

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CollectorCoverageTrackerTest {
    @Test
    fun `ignores stale presence revisions even when they arrive later`() {
        val startedAt = Instant.parse("2026-08-15T10:00:00Z")
        val tracker =
            CollectorCoverageTracker(
                Clock.fixed(startedAt, ZoneOffset.UTC),
            )

        tracker.record(
            activeCollectors = 3,
            occurredAt = startedAt.plusSeconds(2),
            revision = 12,
        )
        tracker.record(
            activeCollectors = 1,
            occurredAt = startedAt.plusSeconds(3),
            revision = 11,
        )

        val coverage =
            tracker.getCoverage(
                from = startedAt,
                to = startedAt.plusSeconds(10),
            )

        assertEquals(true, coverage.covered)
        assertEquals(3, coverage.maximumActiveCollectors)
    }
}
