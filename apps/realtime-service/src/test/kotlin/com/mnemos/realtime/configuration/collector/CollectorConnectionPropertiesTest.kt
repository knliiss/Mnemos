package com.mnemos.realtime.configuration.collector

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class CollectorConnectionPropertiesTest {
    @Test
    fun `keeps an observing collector active for ten minutes by default`() {
        val properties = CollectorConnectionProperties()

        assertEquals(
            Duration.ofMinutes(10),
            properties.observationTimeout,
        )
    }
}
