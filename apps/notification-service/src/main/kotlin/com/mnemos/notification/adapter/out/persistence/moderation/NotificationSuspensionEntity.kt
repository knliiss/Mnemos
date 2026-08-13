package com.mnemos.notification.adapter.out.persistence.moderation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notification_suspensions")
class NotificationSuspensionEntity(
    @Id
    @Column(name = "sanction_id", nullable = false, updatable = false)
    var sanctionId: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,
    @Column(name = "active", nullable = false)
    var active: Boolean,
    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,
)
