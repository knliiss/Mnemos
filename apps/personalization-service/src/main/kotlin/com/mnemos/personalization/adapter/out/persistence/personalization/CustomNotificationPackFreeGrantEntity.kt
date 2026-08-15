package com.mnemos.personalization.adapter.out.persistence.personalization

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "custom_notification_pack_free_grants")
class CustomNotificationPackFreeGrantEntity(
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "claimed_at", nullable = false, updatable = false)
    val claimedAt: Instant,
)
