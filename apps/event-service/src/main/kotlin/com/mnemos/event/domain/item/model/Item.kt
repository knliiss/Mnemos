package com.mnemos.event.domain.item.model

class Item(
    val id: ItemId = ItemId.generate(),
    val name: String,
    val rarity: DomainItemRarity,
    val type: DomainItemType,
)
