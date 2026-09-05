package com.mnemos.event.domain.item.repository

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.Item

interface ItemRepository {
    fun getOrCreate(
        itemKey: String?,
        itemName: String,
        type: DomainItemType,
        rarity: DomainItemRarity,
    ): Item
}
