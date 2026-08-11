package com.mnemos.notification.adapter.out.persistence.subscription

import com.mnemos.notification.domain.subscription.model.SubscriptionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "subscriptions")
class SubscriptionEntity(
    @Id
    var id: UUID,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SubscriptionType,
    @Column(name = "equivalence_key", nullable = false)
    var equivalenceKey: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
