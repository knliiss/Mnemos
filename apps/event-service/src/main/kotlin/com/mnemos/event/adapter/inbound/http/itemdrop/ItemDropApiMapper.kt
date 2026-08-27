package com.mnemos.event.adapter.inbound.http.itemdrop

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponseMapper.toPageResponse
import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.usecase.list.ListItemDropsQuery
import org.springframework.stereotype.Component

@Component
object ItemDropApiMapper {
    fun ListItemDropsRequest.toQuery(): ListItemDropsQuery =
        ListItemDropsQuery(
            droppedFor = droppedFor,
            itemName = itemName,
            itemRarity = itemRarity,
            page =
                PageQuery(
                    page = page,
                    size = size,
                ),
        )

    fun PageResult<ItemDropView>.toResponse(): PageResponse<ItemDropResponse> = toPageResponse { it.toResponse() }

    private fun ItemDropView.toResponse(): ItemDropResponse =
        ItemDropResponse(
            id = id.id,
            itemId = itemId.id,
            itemName = itemName,
            itemRarity = itemRarity,
            itemType = itemType.name,
            playerId = playerId?.id,
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )
}
