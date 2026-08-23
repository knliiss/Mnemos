package com.mnemos.profile.adapter.inbound.http.common.pagination

import com.mnemos.profile.application.common.pagination.PageResult

object PageResponseMapper {
    fun <T, R> PageResult<T>.toPageResponse(transform: (T) -> R): PageResponse<R> =
        PageResponse(
            items = items.map(transform),
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
            hasNextPage = hasNextPage,
        )
}
