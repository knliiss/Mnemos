package com.mnemos.notification.adapter.out.persistence.dungeon

import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "dungeon_subscriptions")
class DungeonSubscriptionEntity(
    @Id
    @Column(name = "subscription_id", nullable = false)
    var subscriptionId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "dungeon_type", nullable = false)
    var dungeonType: DomainDungeonType,
)
