package com.mnemos.playerdirectory.adapter.out.persistence.activity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "pending_player_activity")
class PendingPlayerActivityEntity(
    @Id
    @Column(name = "normalized_nickname", nullable = false, length = 64)
    var normalizedNickname: String,
    @Column(name = "nickname", nullable = false, length = 64)
    var nickname: String,
    @Column(name = "pending_drop_count", nullable = false)
    var pendingDropCount: Long,
    @Column(name = "last_drop_at")
    var lastDropAt: Instant?,
    @Column(name = "next_attempt_at", nullable = false)
    var nextAttemptAt: Instant,
    @Column(name = "lease_until")
    var leaseUntil: Instant?,
    @Column(name = "consecutive_failures", nullable = false)
    var consecutiveFailures: Int,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
