package com.mnemos.event.application.saocatalog.model

data class SaoCatalogItemPage(
    val items: List<SaoCatalogItem>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)
