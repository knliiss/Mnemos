package com.mnemos.notification.adapter.out.persistence.playerpresence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_presence_subscriptions")
class PlayerPresenceSubscriptionEntity(
    @Id
    @Column(name = "subscription_id", nullable = false)
    var subscriptionId: UUID,
    @Column(name = "subscriber_user_id", nullable = false)
    var subscriberUserId: UUID,
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
