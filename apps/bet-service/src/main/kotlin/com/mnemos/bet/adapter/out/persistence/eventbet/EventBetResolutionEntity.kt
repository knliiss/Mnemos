package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetResolutionSource
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "event_bet_round_resolutions")
class EventBetResolutionEntity(
    @Id
    var id: UUID,
    @Column(name = "round_id", nullable = false)
    var roundId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    var outcome: EventBetOutcome,
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    var source: EventBetResolutionSource,
    @Column(name = "resolved_by_user_id")
    var resolvedByUserId: UUID?,
    @Column(name = "reason")
    var reason: String?,
    @Column(name = "resolved_at", nullable = false)
    var resolvedAt: Instant,
)
