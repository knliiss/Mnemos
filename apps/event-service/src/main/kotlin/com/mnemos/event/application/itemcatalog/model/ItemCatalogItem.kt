package com.mnemos.event.application.itemcatalog.model

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.ItemId

data class ItemCatalogItem(
    val id: ItemId,
    val gameKey: String?,
    val name: String,
    val type: DomainItemType,
    val rarity: DomainItemRarity,
)
