package com.mnemos.event.adapter.out.persistence.item

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "items")
class ItemEntity(
    @Id
    var id: UUID,
    @Column(name = "game_key", updatable = false)
    var gameKey: String?,
    @Column(name = "name", nullable = false, updatable = false)
    var name: String,
    @Column(name = "type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    var type: DomainItemType,
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, updatable = false)
    var rarity: DomainItemRarity,
)
