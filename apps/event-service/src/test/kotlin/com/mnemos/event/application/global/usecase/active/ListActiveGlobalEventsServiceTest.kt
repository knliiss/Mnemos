package com.mnemos.event.application.global.usecase.active

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.application.global.port.out.GlobalEventReader
import com.mnemos.event.application.global.usecase.list.ListGlobalEventQuery
import com.mnemos.event.configuration.global.GlobalEventActivityProperties
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class ListActiveGlobalEventsServiceTest {
    private val now = Instant.parse("2026-08-25T16:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val properties =
        GlobalEventActivityProperties(
            activeDuration = Duration.ofMinutes(5),
            maximumSimultaneousEvents = 3,
        )

    @Test
    fun `returns newest active event per type and caps simultaneous events`() {
        val events =
            listOf(
                event(DomainGlobalEventType.DARKNESS, now.minusSeconds(290)),
                event(DomainGlobalEventType.MOON, now.minusSeconds(80)),
                event(DomainGlobalEventType.MOON, now.minusSeconds(20)),
                event(DomainGlobalEventType.ECLIPSE, now.minusSeconds(40)),
                event(DomainGlobalEventType.EXPLOSION, now.minusSeconds(10)),
            )
        val reader = FakeGlobalEventReader(events)
        val service = ListActiveGlobalEventsService(reader, properties, clock)

        val result = service.execute()

        assertEquals(
            listOf(
                DomainGlobalEventType.EXPLOSION,
                DomainGlobalEventType.MOON,
                DomainGlobalEventType.ECLIPSE,
            ),
            result.map { event -> event.type },
        )
        assertEquals(now.plusSeconds(280), result[1].endsAt)
        assertEquals(now.minus(properties.activeDuration), reader.startedAfter)
    }

    @Test
    fun `does not return event that ends on current instant`() {
        val boundaryEvent = event(DomainGlobalEventType.DARKNESS, now.minus(properties.activeDuration))
        val reader = FakeGlobalEventReader(listOf(boundaryEvent))
        val service = ListActiveGlobalEventsService(reader, properties, clock)

        val result = service.execute()

        assertEquals(0, result.size)
    }

    private fun event(
        type: DomainGlobalEventType,
        startedAt: Instant,
    ): GlobalEventView =
        GlobalEventView(
            id = EventId.generate(),
            type = type,
            startedAt = startedAt,
        )

    private class FakeGlobalEventReader(
        private val events: List<GlobalEventView>,
    ) : GlobalEventReader {
        var startedAfter: Instant? = null

        override fun find(query: ListGlobalEventQuery): PageResult<GlobalEventView> = error("Paged lookup is not used by this test")

        override fun findStartedAfter(startedAfter: Instant): List<GlobalEventView> {
            this.startedAfter = startedAfter

            return events.filter { event -> event.startedAt.isAfter(startedAfter) }
        }
    }
}
