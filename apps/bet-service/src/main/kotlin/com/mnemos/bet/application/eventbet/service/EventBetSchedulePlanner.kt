package com.mnemos.bet.application.eventbet.service

import com.mnemos.bet.application.eventbet.model.EventBetScheduleSlot
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class EventBetSchedulePlanner {
    fun slots(
        from: Instant,
        to: Instant,
    ): List<EventBetScheduleSlot> {
        val fromMoscow = from.atZone(MOSCOW).withSecond(0).withNano(0)
        val toMoscow = to.atZone(MOSCOW)
        val start = fromMoscow.minusHours(1)

        return generateSequence(start) { it.plusMinutes(1) }
            .takeWhile { !it.isAfter(toMoscow) }
            .mapNotNull(::toSlot)
            .filter { !it.scheduledAt.isBefore(from) && !it.scheduledAt.isAfter(to) }
            .distinctBy { it.eventType to it.scheduledAt }
            .toList()
    }

    private fun toSlot(time: ZonedDateTime): EventBetScheduleSlot? {
        val eventType =
            when {
                time.minute == 0 && time.hour % 2 == 0 -> DomainBetGlobalEventType.MOON
                time.minute == 0 && time.hour in ECLIPSE_HOURS -> DomainBetGlobalEventType.ECLIPSE
                time.minute == 5 && time.hour % 2 == 1 -> DomainBetGlobalEventType.EXPLOSION
                else -> return null
            }

        return EventBetScheduleSlot(
            eventType = eventType,
            scheduledAt = time.toInstant(),
        )
    }

    companion object {
        private val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
        private val ECLIPSE_HOURS = setOf(3, 9, 15, 21)
    }
}
