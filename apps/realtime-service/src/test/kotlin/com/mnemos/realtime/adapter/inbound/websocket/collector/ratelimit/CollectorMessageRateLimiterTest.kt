package com.mnemos.realtime.adapter.inbound.websocket.collector.ratelimit

import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class CollectorMessageRateLimiterTest {
    @Test
    fun `limits each collector independently and resets after one second`() {
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000001"),
            )
        val otherCollectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000002"),
            )
        val clock = MutableClock(Instant.parse("2026-08-15T10:00:00Z"))
        val limiter =
            CollectorMessageRateLimiter(
                properties =
                    CollectorConnectionProperties(
                        messagesPerSecond = 2,
                    ),
                clock = clock,
            )

        assertTrue(limiter.allow(collectorId))
        assertTrue(limiter.allow(collectorId))
        assertFalse(limiter.allow(collectorId))
        assertTrue(limiter.allow(otherCollectorId))

        clock.current = clock.current.plusSeconds(1)

        assertTrue(limiter.allow(collectorId))
    }

    private class MutableClock(
        var current: Instant,
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId): Clock = this

        override fun instant(): Instant = current
    }
}
