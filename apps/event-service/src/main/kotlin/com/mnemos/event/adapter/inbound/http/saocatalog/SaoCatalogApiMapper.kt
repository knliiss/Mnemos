package com.mnemos.event.adapter.inbound.http.saocatalog

import com.mnemos.event.application.saocatalog.model.SaoCatalogItem
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemPage
import com.mnemos.event.application.saocatalog.service.SearchSaoCatalogItemsQuery

object SaoCatalogApiMapper {
    fun SearchSaoCatalogItemsRequest.toQuery(): SearchSaoCatalogItemsQuery =
        SearchSaoCatalogItemsQuery(
            search = search?.trim()?.takeIf(String::isNotEmpty),
            type = type,
            rarity = rarity,
            locationId = locationId,
            page = page,
            size = size,
        )

    fun SaoCatalogItemPage.toResponse(): SaoCatalogItemPageResponse =
        SaoCatalogItemPageResponse(
            items = items.map(SaoCatalogItem::toResponse),
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
        )

    private fun SaoCatalogItem.toResponse(): SaoCatalogItemResponse =
        SaoCatalogItemResponse(
            gameKey = gameKey,
            defaultName = defaultName,
            type = type,
            rarity = rarity,
            locations =
                locationIds
                    .sorted()
                    .map { locationId ->
                        SaoCatalogItemLocationResponse(
                            id = locationId,
                            nameKey = "sao.location.$locationId.name",
                            descriptionKey = "sao.location.$locationId.description",
                        )
                    },
        )
}
