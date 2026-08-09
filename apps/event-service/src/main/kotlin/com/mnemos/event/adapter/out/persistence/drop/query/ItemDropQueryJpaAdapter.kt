package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.adapter.out.persistence.common.pagination.toPageResult
import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventJpaRepository
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.port.out.ItemDropReader
import com.mnemos.event.application.itemdrop.usecase.list.ListItemDropsQuery
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.ItemId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class ItemDropQueryJpaAdapter(
    private val itemDropEventJpaRepository: ItemDropEventJpaRepository,
) : ItemDropReader {
    override fun find(query: ListItemDropsQuery): PageResult<ItemDropView> {
        val page =
            itemDropEventJpaRepository.findViews(
                droppedFor = query.droppedFor,
                itemName = query.itemName,
                itemRarity = query.itemRarity,
                pageable =
                    PageRequest.of(
                        query.page.page,
                        query.page.size,
                    ),
            )

        return page.toPageResult { it.toView() }
    }

    private fun ItemDropViewProjection.toView(): ItemDropView =
        ItemDropView(
            id = EventId(id),
            itemId = ItemId(itemId),
            itemName = itemName,
            itemRarity = itemRarity,
            itemType = itemType,
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )
}
