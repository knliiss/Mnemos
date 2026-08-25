package com.mnemos.notification.adapter.out.persistence.raid

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "raid_subscriptions")
class RaidSubscriptionEntity(
    @Id
    @Column(name = "subscription_id", nullable = false)
    var subscriptionId: UUID,
    @ElementCollection
    @CollectionTable(
        name = "raid_subscription_locations",
        joinColumns = [JoinColumn(name = "subscription_id")],
    )
    @Column(name = "location", nullable = false)
    var locations: MutableSet<Int>,
)
