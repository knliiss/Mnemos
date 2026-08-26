package com.mnemos.playerdirectory.adapter.out.persistence.tracking

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_tracking_state")
class PlayerTrackingStateEntity(
    @Id
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "active_until")
    var activeUntil: Instant?,
    @Column(name = "next_poll_at", nullable = false)
    var nextPollAt: Instant,
    @Column(name = "lease_until")
    var leaseUntil: Instant?,
    @Column(name = "last_poll_at")
    var lastPollAt: Instant?,
    @Column(name = "consecutive_failures", nullable = false)
    var consecutiveFailures: Int,
    @Column(name = "observed_drop_count", nullable = false)
    var observedDropCount: Long,
    @Column(name = "last_drop_at")
    var lastDropAt: Instant?,
    @Column(name = "last_sao_online_at")
    var lastSaoOnlineAt: Instant?,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
