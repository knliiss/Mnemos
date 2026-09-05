package com.mnemos.event.application.saocatalog.model

import java.time.Instant

data class SaoCatalogItem(
    val gameKey: String,
    val defaultName: String,
    val type: SaoCatalogItemType,
    val rarity: SaoCatalogItemRarity?,
    val styleColor: String?,
    val locationIds: Set<Int>,
    val sourceHash: String,
    val updatedAt: Instant,
)

enum class SaoCatalogItemType {
    WEAPON,
    PET,
    AURA,
    BOOK,
    JEWELRY,
    OTHER,
}

enum class SaoCatalogItemRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY,
    MYTHICAL,
    SECRET,
    OTHER,
}
