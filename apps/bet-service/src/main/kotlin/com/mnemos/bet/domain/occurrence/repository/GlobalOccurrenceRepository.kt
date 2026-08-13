package com.mnemos.bet.domain.occurrence.repository

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.occurrence.model.GlobalOccurrence
import java.time.Instant

interface GlobalOccurrenceRepository {
    fun createIfAbsent(occurrence: GlobalOccurrence)

    fun findFirst(
        eventType: DomainBetGlobalEventType,
        from: Instant,
        to: Instant,
    ): GlobalOccurrence?
}
