package com.mnemos.event.adapter.inbound.http.itemcatalog

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class SearchItemCatalogRequest(
    @field:Size(min = 2, max = 24)
    val search: String,
    @field:Min(1)
    @field:Max(20)
    val limit: Int = 8,
)
