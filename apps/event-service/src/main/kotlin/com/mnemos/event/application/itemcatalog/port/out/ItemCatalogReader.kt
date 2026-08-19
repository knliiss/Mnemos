package com.mnemos.event.application.itemcatalog.port.out

import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem

interface ItemCatalogReader {
    fun search(
        search: String,
        limit: Int,
    ): List<ItemCatalogItem>
}
