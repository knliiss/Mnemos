package com.mnemos.event.application.itemcatalog.usecase.search

data class SearchItemCatalogQuery(
    val search: String,
    val limit: Int,
)
