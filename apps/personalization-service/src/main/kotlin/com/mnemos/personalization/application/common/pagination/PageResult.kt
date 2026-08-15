package com.mnemos.personalization.application.common.pagination

data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)
