package com.mnemos.event.application.global.usecase.active

import com.mnemos.event.application.global.model.ActiveGlobalEventView
import com.mnemos.event.application.global.port.out.GlobalEventReader
import com.mnemos.event.configuration.global.GlobalEventActivityProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional(readOnly = true)
class ListActiveGlobalEventsService(
    private val eventReader: GlobalEventReader,
    private val properties: GlobalEventActivityProperties,
    private val clock: Clock,
) : ListActiveGlobalEventsUseCase {
    override fun execute(): List<ActiveGlobalEventView> {
        val now = clock.instant()
        val startedAfter = now.minus(properties.activeDuration)
        val recentEvents = eventReader.findStartedAfter(startedAfter)
        val projectedEvents =
            recentEvents.map { event ->
                ActiveGlobalEventView(
                    id = event.id,
                    type = event.type,
                    startedAt = event.startedAt,
                    endsAt = event.startedAt.plus(properties.activeDuration),
                )
            }
        val activeEvents = projectedEvents.filter { event -> event.endsAt.isAfter(now) }
        val newestEvents = activeEvents.sortedByDescending { event -> event.startedAt }
        val newestEventsByType = newestEvents.distinctBy { event -> event.type }

        return newestEventsByType.take(properties.maximumSimultaneousEvents)
    }
}
