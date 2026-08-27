package com.mnemos.playerdirectory.adapter.out.persistence.subscription

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "presence_subscriptions")
class PresenceSubscriptionEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "subscriber_user_id", nullable = false)
    var subscriberUserId: UUID,
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
