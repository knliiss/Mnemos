package com.mnemos.bet.application.eventbet.service

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class EventBetSchedulePlannerTest {
    private val planner = EventBetSchedulePlanner()

    @Test
    fun `planner creates Moscow schedule with explosion at minute five`() {
        val from = Instant.parse("2026-08-13T21:00:00Z")
        val to = Instant.parse("2026-08-14T04:10:00Z")

        val slots = planner.slots(from, to)

        assertEquals(
            listOf(
                DomainBetGlobalEventType.MOON to Instant.parse("2026-08-13T21:00:00Z"),
                DomainBetGlobalEventType.EXPLOSION to Instant.parse("2026-08-13T22:05:00Z"),
                DomainBetGlobalEventType.MOON to Instant.parse("2026-08-13T23:00:00Z"),
                DomainBetGlobalEventType.ECLIPSE to Instant.parse("2026-08-14T00:00:00Z"),
                DomainBetGlobalEventType.EXPLOSION to Instant.parse("2026-08-14T00:05:00Z"),
                DomainBetGlobalEventType.MOON to Instant.parse("2026-08-14T01:00:00Z"),
                DomainBetGlobalEventType.EXPLOSION to Instant.parse("2026-08-14T02:05:00Z"),
                DomainBetGlobalEventType.MOON to Instant.parse("2026-08-14T03:00:00Z"),
                DomainBetGlobalEventType.EXPLOSION to Instant.parse("2026-08-14T04:05:00Z"),
            ),
            slots.map { slot -> slot.eventType to slot.scheduledAt },
        )
    }
}
