package com.mnemos.notification.adapter.out.persistence.itemdrop

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "item_drop_subscriptions")
class ItemDropSubscriptionEntity(
    @Id
    @Column(name = "subscription_id")
    var subscriptionId: UUID,
    @Column(name = "dropped_for")
    var droppedFor: String?,
    @Column(name = "item_name")
    var itemName: String?,
)
