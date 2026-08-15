package com.mnemos.personalization.adapter.out.persistence.badge

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_badges")
class UserBadgeEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "badge_id", nullable = false)
    var badgeId: UUID,
    @Column(name = "granted_at", nullable = false)
    var grantedAt: Instant,
    @Column(name = "active", nullable = false)
    var active: Boolean,
)
