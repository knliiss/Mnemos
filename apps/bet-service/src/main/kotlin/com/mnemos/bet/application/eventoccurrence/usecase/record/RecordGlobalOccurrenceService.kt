package com.mnemos.bet.application.eventoccurrence.usecase.record

import com.mnemos.bet.domain.occurrence.model.GlobalOccurrence
import com.mnemos.bet.domain.occurrence.repository.GlobalOccurrenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordGlobalOccurrenceService(
    private val repository: GlobalOccurrenceRepository,
) : RecordGlobalOccurrenceUseCase {
    @Transactional
    override fun execute(command: RecordGlobalOccurrenceCommand) {
        repository.createIfAbsent(
            GlobalOccurrence(
                eventId = command.eventId,
                eventType = command.eventType,
                occurredAt = command.occurredAt,
            ),
        )
    }
}
