package com.mnemos.event.adapter.out.memory.collector

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CollectorCoverageTrackerTest {
    @Test
    fun `coverage is true when collector is active at start of window`() {
        val instant = Instant.parse("2026-08-13T16:00:00Z")
        val tracker = CollectorCoverageTracker(Clock.fixed(instant, ZoneOffset.UTC))

        tracker.record(1)

        val coverage =
            tracker.getCoverage(
                from = instant,
                to = instant.plusSeconds(30),
            )

        assertTrue(coverage.covered)
        assertEquals(1, coverage.maximumActiveCollectors)
    }

    @Test
    fun `coverage is false for time before service startup`() {
        val startedAt = Instant.parse("2026-08-13T16:00:00Z")
        val tracker = CollectorCoverageTracker(Clock.fixed(startedAt, ZoneOffset.UTC))

        tracker.record(2)

        val coverage =
            tracker.getCoverage(
                from = startedAt.minusSeconds(60),
                to = startedAt.plusSeconds(30),
            )

        assertFalse(coverage.covered)
        assertEquals(0, coverage.maximumActiveCollectors)
    }
}
