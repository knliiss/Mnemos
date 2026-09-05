package com.mnemos.event.adapter.inbound.http.saocatalog

import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class SearchSaoCatalogItemsRequest(
    val search: String? = null,
    val type: SaoCatalogItemType? = null,
    val rarity: SaoCatalogItemRarity? = null,
    val locationId: Int? = null,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(200)
    val size: Int = 50,
)
