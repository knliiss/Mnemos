package com.mnemos.event.adapter.out.persistence.common.pagination

import com.mnemos.event.application.common.pagination.PageResult
import org.springframework.data.domain.Page

object PagePersistenceMapper {
    fun <T : Any, R> Page<T>.toPageResult(transform: (T) -> R): PageResult<R> =
        PageResult(
            items = content.map(transform),
            page = number,
            size = size,
            totalItems = totalElements,
            totalPages = totalPages,
            hasNextPage = hasNext(),
        )
}
