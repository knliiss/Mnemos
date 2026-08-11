package com.mnemos.notification.adapter.out.persistence.booster

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "booster_subscriptions")
class BoosterSubscriptionEntity(
    @Id
    @Column(name = "subscription_id")
    var subscriptionId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "booster_type", nullable = false)
    var boosterType: DomainBoosterType,
)
