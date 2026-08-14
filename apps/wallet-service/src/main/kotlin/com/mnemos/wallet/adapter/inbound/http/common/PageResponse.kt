package com.mnemos.wallet.adapter.inbound.http.common

data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)
