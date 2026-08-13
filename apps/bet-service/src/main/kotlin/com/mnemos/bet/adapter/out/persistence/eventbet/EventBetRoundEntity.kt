package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "event_bet_rounds")
class EventBetRoundEntity(
    @Id
    var id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: DomainBetGlobalEventType,
    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: Instant,
    @Column(name = "closes_at", nullable = false)
    var closesAt: Instant,
    @Column(name = "resolves_at", nullable = false)
    var resolvesAt: Instant,
    @Column(name = "yes_odds", nullable = false, precision = 8, scale = 4)
    var yesOdds: BigDecimal,
    @Column(name = "no_odds", nullable = false, precision = 8, scale = 4)
    var noOdds: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: EventBetRoundStatus,
    @Enumerated(EnumType.STRING)
    @Column(name = "effective_outcome")
    var effectiveOutcome: EventBetOutcome?,
)
