package com.mnemos.event.application.itemdrop.usecase.list

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.port.out.ItemDropReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListItemDropsService(
    private val itemDropReader: ItemDropReader,
) : ListItemDropsUseCase {
    override fun execute(query: ListItemDropsQuery): PageResult<ItemDropView> = itemDropReader.find(query)
}
