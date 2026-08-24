package com.mnemos.event.adapter.out.persistence.wipe

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "wipes")
class WipeEntity(
    @Id
    var id: UUID,
    @Column(name = "number", nullable = false)
    var number: Int,
    @Column(name = "name")
    var name: String?,
    @Column(name = "started_at", nullable = false)
    var startedAt: Instant,
    @Column(name = "ended_at")
    var endedAt: Instant?,
    @Column(name = "created_by", nullable = false)
    var createdBy: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
