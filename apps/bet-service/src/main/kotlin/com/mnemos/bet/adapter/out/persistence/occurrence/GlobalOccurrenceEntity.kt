package com.mnemos.bet.adapter.out.persistence.occurrence

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "global_occurrences")
class GlobalOccurrenceEntity(
    @Id
    @Column(name = "event_id")
    var eventId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: DomainBetGlobalEventType,
    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,
)
