package com.mnemos.event.adapter.out.persistence.saocatalog

import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "sao_catalog_items")
class SaoCatalogItemEntity(
    @Id
    @Column(name = "game_key", nullable = false, length = 160)
    var gameKey: String,
    @Column(name = "default_name", nullable = false, length = 128)
    var defaultName: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 32)
    var type: SaoCatalogItemType,
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", length = 32)
    var rarity: SaoCatalogItemRarity?,
    @Column(name = "style_color", length = 7)
    var styleColor: String?,
    @Column(name = "active", nullable = false)
    var active: Boolean,
    @Column(name = "source_hash", nullable = false, length = 64)
    var sourceHash: String,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "sao_catalog_item_locations",
        joinColumns = [JoinColumn(name = "game_key", referencedColumnName = "game_key")],
    )
    @Column(name = "location_id", nullable = false)
    var locationIds: MutableSet<Int> = mutableSetOf(),
)
