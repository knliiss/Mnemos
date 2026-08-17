package com.mnemos.event.application.itemdrop.port.out

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.usecase.list.ListItemDropsQuery
import com.mnemos.event.domain.EventId

interface ItemDropReader {
    fun find(query: ListItemDropsQuery): PageResult<ItemDropView>

    fun findById(id: EventId): ItemDropView?
}
