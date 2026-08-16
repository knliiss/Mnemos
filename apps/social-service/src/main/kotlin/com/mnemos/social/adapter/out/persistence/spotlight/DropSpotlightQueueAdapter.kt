package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.application.spotlight.port.out.DropSpotlightQueue
import com.mnemos.social.application.spotlight.port.out.DropSpotlightReservation
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DropSpotlightQueueAdapter(
    private val queueStateRepository: SpotlightQueueStateJpaRepository,
    private val spotlightRepository: DropSpotlightJpaRepository,
    private val properties: DropSpotlightProperties,
) : DropSpotlightQueue {
    override fun reserve(now: Instant): DropSpotlightReservation {
        val state = queueStateRepository.findForUpdate()
        val latestScheduledAt =
            spotlightRepository
                .findTopByStatusInOrderByScheduledAtDesc(QUEUE_OCCUPYING_STATUSES)
                ?.scheduledAt
        val earliestAfterPrevious = latestScheduledAt?.plus(properties.globalGap)
        val scheduledAt =
            if (earliestAfterPrevious != null && earliestAfterPrevious.isAfter(now)) {
                earliestAfterPrevious
            } else {
                now
            }
        val sequence = state.nextSequence

        state.nextSequence = sequence + 1
        queueStateRepository.save(state)

        return DropSpotlightReservation(
            sequence = sequence,
            scheduledAt = scheduledAt,
        )
    }

    private companion object {
        val QUEUE_OCCUPYING_STATUSES =
            listOf(
                DropSpotlightStatus.PENDING_PAYMENT,
                DropSpotlightStatus.QUEUED,
                DropSpotlightStatus.PUBLISHED,
            )
    }
}
