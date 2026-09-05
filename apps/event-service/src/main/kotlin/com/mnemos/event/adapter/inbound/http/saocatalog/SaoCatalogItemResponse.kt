package com.mnemos.event.adapter.inbound.http.saocatalog

import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType

data class SaoCatalogItemResponse(
    val gameKey: String,
    val defaultName: String,
    val type: SaoCatalogItemType,
    val rarity: SaoCatalogItemRarity?,
    val styleColor: String?,
    val locations: List<SaoCatalogItemLocationResponse>,
)

data class SaoCatalogItemLocationResponse(
    val id: Int,
    val nameKey: String,
    val descriptionKey: String,
)

data class SaoCatalogItemPageResponse(
    val items: List<SaoCatalogItemResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)
