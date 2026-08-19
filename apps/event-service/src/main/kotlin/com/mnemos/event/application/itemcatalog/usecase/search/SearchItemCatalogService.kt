package com.mnemos.event.application.itemcatalog.usecase.search

import com.mnemos.event.application.itemcatalog.model.ItemCatalogItem
import com.mnemos.event.application.itemcatalog.port.out.ItemCatalogReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SearchItemCatalogService(
    private val itemCatalogReader: ItemCatalogReader,
) {
    fun execute(query: SearchItemCatalogQuery): List<ItemCatalogItem> {
        val normalizedQuery =
            query.copy(
                search = query.search.trim(),
                limit = query.limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
            )

        if (normalizedQuery.search.length < MIN_SEARCH_LENGTH) {
            return emptyList()
        }

        return itemCatalogReader.search(normalizedQuery)
    }

    private companion object {
        const val MIN_SEARCH_LENGTH = 2
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 20
    }
}
