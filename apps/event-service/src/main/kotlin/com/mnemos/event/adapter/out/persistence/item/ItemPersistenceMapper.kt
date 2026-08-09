package com.mnemos.event.adapter.out.persistence.item

import com.mnemos.event.domain.item.model.Item
import com.mnemos.event.domain.item.model.ItemId

object ItemPersistenceMapper {
    fun ItemEntity.toDomain(): Item =
        Item(
            id = ItemId(id),
            name = name,
            type = type,
            rarity = rarity,
        )
}
