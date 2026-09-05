package com.mnemos.event.adapter.inbound.http.saocatalog

import com.mnemos.event.adapter.inbound.http.saocatalog.SaoCatalogApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.saocatalog.SaoCatalogApiMapper.toResponse
import com.mnemos.event.application.saocatalog.service.SearchSaoCatalogItemsService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events/catalog/items")
class SaoCatalogController(
    private val searchService: SearchSaoCatalogItemsService,
) {
    @GetMapping
    fun search(
        @Valid @ModelAttribute request: SearchSaoCatalogItemsRequest,
    ): SaoCatalogItemPageResponse = searchService.execute(request.toQuery()).toResponse()
}
