package com.mnemos.bet.adapter.out.persistence.occurrence

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface GlobalOccurrenceJpaRepository : JpaRepository<GlobalOccurrenceEntity, UUID> {
    @Query(
        "SELECT o FROM GlobalOccurrenceEntity o " +
            "WHERE o.eventType = :eventType AND o.occurredAt >= :from AND o.occurredAt <= :to " +
            "ORDER BY o.occurredAt ASC",
    )
    fun findFirstInWindow(
        @Param("eventType") eventType: DomainBetGlobalEventType,
        @Param("from") from: Instant,
        @Param("to") to: Instant,
    ): List<GlobalOccurrenceEntity>
}
