package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.application.spotlight.port.out.DropSpotlightQueue
import com.mnemos.social.application.spotlight.port.out.DropSpotlightReservation
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DropSpotlightQueueAdapter(
    private val repository: SpotlightQueueStateJpaRepository,
    private val properties: DropSpotlightProperties,
) : DropSpotlightQueue {
    override fun reserve(now: Instant): DropSpotlightReservation {
        val state = repository.findForUpdate()
        val earliestAfterPrevious = state.lastReservedAt?.plus(properties.globalGap)
        val scheduledAt = if (earliestAfterPrevious != null && earliestAfterPrevious.isAfter(now)) earliestAfterPrevious else now
        val sequence = state.nextSequence

        state.lastReservedAt = scheduledAt
        state.nextSequence = sequence + 1
        repository.save(state)

        return DropSpotlightReservation(
            sequence = sequence,
            scheduledAt = scheduledAt,
        )
    }
}
