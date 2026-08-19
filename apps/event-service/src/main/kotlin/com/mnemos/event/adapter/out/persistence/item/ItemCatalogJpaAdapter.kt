package com.mnemos.event.adapter.out.persistence.item

import com.mnemos.event.adapter.out.persistence.item.ItemPersistenceMapper.toCatalogItem
import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem
import com.mnemos.event.application.itemcatalog.port.out.ItemCatalogReader
import com.mnemos.event.application.itemcatalog.usecase.search.SearchItemCatalogQuery
import org.springframework.stereotype.Repository

@Repository
class ItemCatalogJpaAdapter(
    private val repository: ItemJpaRepository,
) : ItemCatalogReader {
    override fun search(query: SearchItemCatalogQuery): List<ItemCatalogItem> =
        repository
            .searchCatalog(
                search = query.search,
                limit = query.limit,
            ).map { entity -> entity.toCatalogItem() }
}
