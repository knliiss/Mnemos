package com.mnemos.event.application.itemdrop.usecase.list

import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.domain.item.model.DomainItemRarity

data class ListItemDropsQuery(
    val droppedFor: String?,
    val itemName: String?,
    val itemRarity: DomainItemRarity?,
    val page: PageQuery,
)
