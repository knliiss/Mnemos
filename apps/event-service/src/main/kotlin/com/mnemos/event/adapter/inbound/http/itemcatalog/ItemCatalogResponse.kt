package com.mnemos.event.adapter.inbound.http.itemcatalog

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import java.util.UUID

data class ItemCatalogResponse(
    val id: UUID,
    val gameKey: String?,
    val name: String,
    val type: DomainItemType,
    val rarity: DomainItemRarity,
)
