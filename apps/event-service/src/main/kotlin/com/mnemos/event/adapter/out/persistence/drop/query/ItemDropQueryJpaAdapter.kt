package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.adapter.out.persistence.common.pagination.PagePersistenceMapper.toPageResult
import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventJpaRepository
import com.mnemos.event.adapter.out.persistence.drop.query.ItemDropQueryMapper.toView
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.port.out.ItemDropReader
import com.mnemos.event.application.itemdrop.usecase.list.ListItemDropsQuery
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class ItemDropQueryJpaAdapter(
    private val itemDropEventJpaRepository: ItemDropEventJpaRepository,
    private val playerIdentityRepository: PlayerIdentityProjectionRepository,
) : ItemDropReader {
    override fun find(query: ListItemDropsQuery): PageResult<ItemDropView> {
        val playerId = query.droppedFor?.let(playerIdentityRepository::findCurrentOwnerId)

        if (query.droppedFor != null && playerId == null) {
            return emptyPage(query)
        }

        val page =
            itemDropEventJpaRepository.findViews(
                playerId = playerId?.id,
                itemName = query.itemName,
                itemRarity = query.itemRarity?.name,
                pageable = PageRequest.of(query.page.page, query.page.size),
            )

        return page.toPageResult { projection ->
            projection.toView().withCurrentNickname()
        }
    }

    override fun findById(id: EventId): ItemDropView? =
        itemDropEventJpaRepository.findViewById(id.id)?.toView()?.withCurrentNickname()

    private fun ItemDropView.withCurrentNickname(): ItemDropView {
        val id: CristalixPlayerId = playerId ?: return this
        val current = playerIdentityRepository.findCurrentNickname(id) ?: return this
        return copy(droppedFor = current)
    }

    private fun emptyPage(query: ListItemDropsQuery): PageResult<ItemDropView> =
        PageResult(
            items = emptyList(),
            page = query.page.page,
            size = query.page.size,
            totalItems = 0,
            totalPages = 0,
            hasNextPage = false,
        )
}
