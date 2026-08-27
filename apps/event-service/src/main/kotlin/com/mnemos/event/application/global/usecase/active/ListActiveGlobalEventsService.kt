package com.mnemos.event.application.global.usecase.active

import com.mnemos.event.application.global.model.ActiveGlobalEventView
import com.mnemos.event.application.global.port.out.GlobalEventReader
import com.mnemos.event.configuration.global.GlobalEventActivityProperties
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration

@Service
@Transactional(readOnly = true)
class ListActiveGlobalEventsService(
    private val eventReader: GlobalEventReader,
    private val properties: GlobalEventActivityProperties,
    private val clock: Clock,
) : ListActiveGlobalEventsUseCase {
    override fun execute(): List<ActiveGlobalEventView> {
        val now = clock.instant()
        val startedAfter = now.minus(properties.maximumActiveDuration)
        val recentEvents = eventReader.findStartedAfter(startedAfter)
        val projectedEvents =
            recentEvents.map { event ->
                ActiveGlobalEventView(
                    id = event.id,
                    type = event.type,
                    startedAt = event.startedAt,
                    endsAt = event.startedAt.plus(activeDurationFor(event.type)),
                )
            }
        val activeEvents = projectedEvents.filter { event -> event.endsAt.isAfter(now) }
        val newestEvents = activeEvents.sortedByDescending { event -> event.startedAt }
        val newestEventsByType = newestEvents.distinctBy { event -> event.type }

        return newestEventsByType.take(properties.maximumSimultaneousEvents)
    }

    private fun activeDurationFor(type: DomainGlobalEventType): Duration =
        when (type) {
            DomainGlobalEventType.DARKNESS -> properties.activeDuration
            DomainGlobalEventType.MOON -> properties.moonActiveDuration
            DomainGlobalEventType.ECLIPSE -> properties.eclipseActiveDuration
            DomainGlobalEventType.EXPLOSION -> properties.activeDuration
        }
}
