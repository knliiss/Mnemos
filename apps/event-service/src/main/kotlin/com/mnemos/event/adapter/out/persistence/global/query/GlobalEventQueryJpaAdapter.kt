package com.mnemos.event.adapter.out.persistence.global.query

import com.mnemos.event.adapter.out.persistence.common.pagination.toPageResult
import com.mnemos.event.adapter.out.persistence.global.GlobalEventJpaRepository
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.application.global.port.out.GlobalEventReader
import com.mnemos.event.application.global.usecase.list.ListGlobalEventQuery
import com.mnemos.event.domain.EventId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class GlobalEventQueryJpaAdapter(
    private val eventJpaRepository: GlobalEventJpaRepository,
) : GlobalEventReader {
    override fun find(query: ListGlobalEventQuery): PageResult<GlobalEventView> {
        val pageable =
            PageRequest.of(
                query.page.page,
                query.page.size,
            )

        val page =
            if (query.types.isEmpty()) {
                eventJpaRepository.findViews(pageable)
            } else {
                eventJpaRepository.findViewsByTypes(
                    types = query.types,
                    pageable = pageable,
                )
            }

        return page.toPageResult { it.toView() }
    }

    private fun GlobalEventViewProjection.toView(): GlobalEventView =
        GlobalEventView(
            id = EventId(id),
            type = type,
            startedAt = startedAt,
        )
}
