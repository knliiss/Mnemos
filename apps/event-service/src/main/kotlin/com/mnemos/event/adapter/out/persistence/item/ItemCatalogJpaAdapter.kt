package com.mnemos.event.adapter.out.persistence.item

import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem
import com.mnemos.event.application.itemcatalog.port.out.ItemCatalogReader
import com.mnemos.event.domain.item.model.ItemId
import org.springframework.stereotype.Repository

@Repository
class ItemCatalogJpaAdapter(
    private val repository: ItemJpaRepository,
) : ItemCatalogReader {
    override fun search(
        search: String,
        limit: Int,
    ): List<ItemCatalogItem> =
        repository
            .searchCatalog(
                search = search,
                limit = limit,
            ).map { entity ->
                ItemCatalogItem(
                    id = ItemId(entity.id),
                    name = entity.name,
                    type = entity.type,
                    rarity = entity.rarity,
                )
            }
}
