package com.mnemos.event.adapter.out.persistence.raid.query

import com.mnemos.event.adapter.out.persistence.common.pagination.toPageResult
import com.mnemos.event.adapter.out.persistence.raid.RaidEventJpaRepository
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.raid.model.RaidEventView
import com.mnemos.event.application.raid.port.out.RaidEventReader
import com.mnemos.event.application.raid.usecase.list.ListRaidEventQuery
import com.mnemos.event.domain.EventId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class RaidEventQueryJpaRepository(
    private val jpaRepository: RaidEventJpaRepository,
) : RaidEventReader {
    override fun find(query: ListRaidEventQuery): PageResult<RaidEventView> {
        val page =
            jpaRepository.findViews(
                minLocation = query.minLocation,
                maxLocation = query.maxLocation,
                pageable =
                    PageRequest.of(
                        query.page.page,
                        query.page.size,
                    ),
            )

        return page.toPageResult { it.toView() }
    }

    private fun RaidEventViewProjection.toView(): RaidEventView =
        RaidEventView(
            id = EventId(id),
            locations = locations,
            startedAt = startedAt,
        )
}
