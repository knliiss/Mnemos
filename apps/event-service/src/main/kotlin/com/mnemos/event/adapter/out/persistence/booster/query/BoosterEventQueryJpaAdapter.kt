package com.mnemos.event.adapter.out.persistence.booster.query

import com.mnemos.event.adapter.out.persistence.booster.BoosterEventJpaRepository
import com.mnemos.event.adapter.out.persistence.booster.query.BoosterEventQueryMapper.toView
import com.mnemos.event.adapter.out.persistence.common.pagination.PagePersistenceMapper.toPageResult
import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.application.booster.port.out.BoosterEventReader
import com.mnemos.event.application.booster.usecase.list.ListBoosterEventQuery
import com.mnemos.event.application.common.pagination.PageResult
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class BoosterEventQueryJpaAdapter(
    private val jpaRepository: BoosterEventJpaRepository,
) : BoosterEventReader {
    override fun find(query: ListBoosterEventQuery): PageResult<BoosterEventView> {
        val page =
            jpaRepository.findViews(
                type = query.type,
                activatedByName = query.activatedByName,
                pageable =
                    PageRequest.of(
                        query.page.page,
                        query.page.size,
                    ),
            )

        return page.toPageResult { it.toView() }
    }
}
