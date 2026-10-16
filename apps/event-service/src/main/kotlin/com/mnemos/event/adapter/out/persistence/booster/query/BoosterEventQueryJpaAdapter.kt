package com.mnemos.event.adapter.out.persistence.booster.query

import com.mnemos.event.adapter.out.persistence.booster.BoosterEventJpaRepository
import com.mnemos.event.adapter.out.persistence.booster.query.BoosterEventQueryMapper.toView
import com.mnemos.event.adapter.out.persistence.common.pagination.PagePersistenceMapper.toPageResult
import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.application.booster.port.out.BoosterEventReader
import com.mnemos.event.application.booster.usecase.list.ListBoosterEventQuery
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class BoosterEventQueryJpaAdapter(
    private val jpaRepository: BoosterEventJpaRepository,
    private val playerIdentityRepository: PlayerIdentityProjectionRepository,
) : BoosterEventReader {
    override fun find(query: ListBoosterEventQuery): PageResult<BoosterEventView> {
        val playerId = query.activatedByName?.let(playerIdentityRepository::findCurrentOwnerId)

        if (query.activatedByName != null && playerId == null) {
            return emptyPage(query)
        }

        val page =
            jpaRepository.findViews(
                type = query.type,
                playerId = playerId?.id,
                pageable =
                    PageRequest.of(
                        query.page.page,
                        query.page.size,
                    ),
            )

        return page.toPageResult { projection ->
            val view = projection.toView()
            val playerId = view.playerId
            val current = playerId?.let(playerIdentityRepository::findCurrentNickname)
            if (current != null) view.copy(activatedBy = current) else view
        }
    }

    private fun emptyPage(query: ListBoosterEventQuery): PageResult<BoosterEventView> =
        PageResult(
            items = emptyList(),
            page = query.page.page,
            size = query.page.size,
            totalItems = 0,
            totalPages = 0,
            hasNextPage = false,
        )
}
