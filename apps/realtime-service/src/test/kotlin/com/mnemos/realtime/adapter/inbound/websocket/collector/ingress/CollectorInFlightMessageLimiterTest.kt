package com.mnemos.realtime.adapter.inbound.websocket.collector.ingress

import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class CollectorInFlightMessageLimiterTest {
    @Test
    fun `capacity is restored only after an in flight message completes`() {
        val limiter =
            CollectorInFlightMessageLimiter(
                CollectorConnectionProperties(
                    maxInFlightMessages = 2,
                ),
            )
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000210"),
            )

        assertTrue(limiter.tryAcquire(collectorId))
        assertTrue(limiter.tryAcquire(collectorId))
        assertFalse(limiter.tryAcquire(collectorId))

        limiter.release(collectorId)

        assertTrue(limiter.tryAcquire(collectorId))
    }
}
