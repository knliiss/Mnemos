package com.mnemos.event.adapter.out.persistence.booster

import com.mnemos.event.domain.booster.model.DomainBoosterType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "booster_events")
class BoosterEventEntity(
    @Id
    var id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: DomainBoosterType,
    @Column(name = "activated_by", nullable = false)
    var activatedBy: String,
    @Column(name = "activated_at", nullable = false)
    var activatedAt: Instant,
)
