package com.mnemos.event.adapter.out.persistence.global

import com.mnemos.event.domain.global.model.DomainGlobalEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "global_events")
class GlobalEventEntity(
    @Id
    var id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: DomainGlobalEventType,
    @Column(name = "started_at", nullable = false)
    var startedAt: Instant,
)
