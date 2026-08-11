package com.mnemos.notification.adapter.inbound.http.common.pagination

import com.mnemos.notification.application.common.pagination.PageResult

fun <T, R> PageResult<T>.toPageResponse(transform: (T) -> R): PageResponse<R> =
    PageResponse(
        items = items.map(transform),
        page = page,
        size = size,
        totalItems = totalItems,
        totalPages = totalPages,
        hasNextPage = hasNextPage,
    )
