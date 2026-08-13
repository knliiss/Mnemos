package com.mnemos.bet.adapter.out.persistence.occurrence

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.occurrence.model.ExternalEventId
import com.mnemos.bet.domain.occurrence.model.GlobalOccurrence
import com.mnemos.bet.domain.occurrence.repository.GlobalOccurrenceRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class GlobalOccurrenceRepositoryAdapter(
    private val jpaRepository: GlobalOccurrenceJpaRepository,
) : GlobalOccurrenceRepository {
    override fun createIfAbsent(occurrence: GlobalOccurrence) {
        if (jpaRepository.existsById(occurrence.eventId.id)) {
            return
        }

        try {
            jpaRepository.save(
                GlobalOccurrenceEntity(
                    eventId = occurrence.eventId.id,
                    eventType = occurrence.eventType,
                    occurredAt = occurrence.occurredAt,
                ),
            )
        } catch (_: DataIntegrityViolationException) {
        }
    }

    override fun findFirst(
        eventType: DomainBetGlobalEventType,
        from: Instant,
        to: Instant,
    ): GlobalOccurrence? =
        jpaRepository
            .findFirstInWindow(eventType, from, to)
            .firstOrNull()
            ?.let {
                GlobalOccurrence(
                    eventId = ExternalEventId(it.eventId),
                    eventType = it.eventType,
                    occurredAt = it.occurredAt,
                )
            }
}
