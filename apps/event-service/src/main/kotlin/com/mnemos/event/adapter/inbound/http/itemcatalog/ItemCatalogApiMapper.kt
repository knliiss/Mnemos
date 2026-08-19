package com.mnemos.event.adapter.inbound.http.itemcatalog

import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem
import com.mnemos.event.application.itemcatalog.usecase.search.SearchItemCatalogQuery

object ItemCatalogApiMapper {
    fun SearchItemCatalogRequest.toQuery(): SearchItemCatalogQuery =
        SearchItemCatalogQuery(
            search = search,
            limit = limit,
        )

    fun ItemCatalogItem.toResponse(): ItemCatalogResponse =
        ItemCatalogResponse(
            id = id.id,
            name = name,
            type = type,
            rarity = rarity,
        )
}
