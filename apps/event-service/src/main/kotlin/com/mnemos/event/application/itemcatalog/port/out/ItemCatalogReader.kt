package com.mnemos.event.application.itemcatalog.port.out

import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem
import com.mnemos.event.application.itemcatalog.usecase.search.SearchItemCatalogQuery

interface ItemCatalogReader {
    fun search(query: SearchItemCatalogQuery): List<ItemCatalogItem>
}
