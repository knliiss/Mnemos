package com.mnemos.event.adapter.inbound.http.itemcatalog

import com.mnemos.event.adapter.inbound.http.itemcatalog.ItemCatalogApiMapper.toResponse
import com.mnemos.event.application.itemcatalog.usecase.search.SearchItemCatalogService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v1/events/items")
class ItemCatalogController(
    private val searchItemCatalogService: SearchItemCatalogService,
) {
    @GetMapping
    fun search(
        @RequestParam
        @Size(min = 2, max = 24)
        search: String,
        @RequestParam(defaultValue = "8")
        @Min(1)
        @Max(20)
        limit: Int,
    ): List<ItemCatalogResponse> =
        searchItemCatalogService
            .execute(
                search = search,
                limit = limit,
            ).map { item -> item.toResponse() }
}
