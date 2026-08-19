package com.mnemos.event.adapter.inbound.http.itemcatalog

import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem

object ItemCatalogApiMapper {
    fun ItemCatalogItem.toResponse(): ItemCatalogResponse =
        ItemCatalogResponse(
            id = id.id,
            name = name,
            type = type,
            rarity = rarity,
        )
}
