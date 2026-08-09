package com.mnemos.event.adapter.out.persistence.raid

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "raid_events")
class RaidEventEntity(
    @Id
    var id: UUID,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "raid_event_locations",
        joinColumns = [JoinColumn(name = "raid_event_id")],
    )
    @Column(name = "location", nullable = false)
    var locations: Set<Int>,
    @Column(name = "started_at", nullable = false)
    var startedAt: Instant,
)
