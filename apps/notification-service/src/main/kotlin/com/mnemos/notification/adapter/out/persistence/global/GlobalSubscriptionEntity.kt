package com.mnemos.notification.adapter.out.persistence.global

import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "global_subscriptions")
class GlobalSubscriptionEntity(
    @Id
    @Column(name = "subscription_id")
    var subscriptionId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: DomainGlobalEventType,
)
