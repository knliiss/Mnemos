package com.mnemos.event.application.saocatalog.service

import com.mnemos.event.application.saocatalog.model.SaoCatalogItemPage
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import com.mnemos.event.application.saocatalog.port.out.SaoCatalogItemRepository
import org.springframework.stereotype.Service

@Service
class SearchSaoCatalogItemsService(
    private val repository: SaoCatalogItemRepository,
) {
    fun execute(query: SearchSaoCatalogItemsQuery): SaoCatalogItemPage =
        repository.search(
            search = query.search,
            type = query.type,
            rarity = query.rarity,
            locationId = query.locationId,
            page = query.page,
            size = query.size,
        )
}

data class SearchSaoCatalogItemsQuery(
    val search: String?,
    val type: SaoCatalogItemType?,
    val rarity: SaoCatalogItemRarity?,
    val locationId: Int?,
    val page: Int,
    val size: Int,
)
