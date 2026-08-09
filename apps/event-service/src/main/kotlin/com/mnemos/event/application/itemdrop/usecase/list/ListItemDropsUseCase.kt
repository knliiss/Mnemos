package com.mnemos.event.application.itemdrop.usecase.list

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView

interface ListItemDropsUseCase {
    fun execute(query: ListItemDropsQuery): PageResult<ItemDropView>
}
