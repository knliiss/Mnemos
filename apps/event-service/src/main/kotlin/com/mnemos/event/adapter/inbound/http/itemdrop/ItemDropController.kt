package com.mnemos.event.adapter.inbound.http.itemdrop

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.itemdrop.ItemDropApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.itemdrop.ItemDropApiMapper.toResponse
import com.mnemos.event.application.itemdrop.usecase.list.ListItemDropsUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/item-drop-events")
class ItemDropController(
    private val listItemDropsUseCase: ListItemDropsUseCase,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListItemDropsRequest,
    ): PageResponse<ItemDropResponse> {
        val query = request.toQuery()
        return listItemDropsUseCase.execute(query).toResponse()
    }
}
