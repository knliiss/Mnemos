package com.mnemos.event.adapter.inbound.http.itemcatalog

import com.mnemos.event.adapter.inbound.http.itemcatalog.ItemCatalogApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.itemcatalog.ItemCatalogApiMapper.toResponse
import com.mnemos.event.application.itemcatalog.usecase.search.SearchItemCatalogService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events/items")
class ItemCatalogController(
    private val searchItemCatalogService: SearchItemCatalogService,
) {
    @GetMapping
    fun search(
        @Valid @ModelAttribute request: SearchItemCatalogRequest,
    ): List<ItemCatalogResponse> =
        searchItemCatalogService
            .execute(request.toQuery())
            .map { item -> item.toResponse() }
}
