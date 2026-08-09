package com.mnemos.event.application.itemdrop.port.out

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.usecase.list.ListItemDropsQuery

interface ItemDropReader {
    fun find(query: ListItemDropsQuery): PageResult<ItemDropView>
}
