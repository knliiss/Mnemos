package com.mnemos.event.application.saocatalog.port.out

import com.mnemos.event.application.saocatalog.model.SaoCatalogItem
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemPage
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType

interface SaoCatalogItemRepository {
    fun replaceAll(items: Collection<SaoCatalogItem>)

    fun search(
        search: String?,
        type: SaoCatalogItemType?,
        rarity: SaoCatalogItemRarity?,
        locationId: Int?,
        page: Int,
        size: Int,
    ): SaoCatalogItemPage
}
