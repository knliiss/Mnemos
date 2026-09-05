package com.mnemos.event.application.saocatalog.port.out

import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.ItemId

data class LegacyItemIdentity(
    val itemId: ItemId,
    val name: String,
    val type: DomainItemType,
    val gameKey: String?,
)

interface LegacyItemIdentityRepository {
    fun findAll(): List<LegacyItemIdentity>

    fun saveCanonicalIdentity(
        itemId: ItemId,
        gameKey: String,
    )
}
