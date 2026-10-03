package com.mnemos.notification.adapter.out.persistence.ping

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ping_notification_preferences")
class PingNotificationPreferenceEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
