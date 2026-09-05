package com.mnemos.event.adapter.out.persistence.saocatalog

import com.mnemos.event.application.saocatalog.model.SaoCatalogItem
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemPage
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import com.mnemos.event.application.saocatalog.port.out.SaoCatalogItemRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SaoCatalogItemRepositoryAdapter(
    private val repository: SaoCatalogItemJpaRepository,
) : SaoCatalogItemRepository {
    @Transactional
    override fun replaceAll(items: Collection<SaoCatalogItem>) {
        repository.deactivateAll()

        val entities =
            items.map { item ->
                val entity =
                    repository.findById(item.gameKey).orElseGet {
                        SaoCatalogItemEntity(
                            gameKey = item.gameKey,
                            defaultName = item.defaultName,
                            type = item.type,
                            rarity = item.rarity,
                            styleColor = item.styleColor,
                            active = true,
                            sourceHash = item.sourceHash,
                            updatedAt = item.updatedAt,
                        )
                    }

                entity.defaultName = item.defaultName
                entity.type = item.type
                entity.rarity = item.rarity
                entity.styleColor = item.styleColor
                entity.active = true
                entity.sourceHash = item.sourceHash
                entity.updatedAt = item.updatedAt
                entity.locationIds.clear()
                entity.locationIds.addAll(item.locationIds)
                entity
            }

        repository.saveAll(entities)
    }

    @Transactional(readOnly = true)
    override fun search(
        search: String?,
        type: SaoCatalogItemType?,
        rarity: SaoCatalogItemRarity?,
        locationId: Int?,
        page: Int,
        size: Int,
    ): SaoCatalogItemPage {
        val result =
            repository.search(
                search = search?.trim()?.lowercase()?.takeIf(String::isNotEmpty),
                type = type?.name,
                rarity = rarity?.name,
                locationId = locationId,
                pageable = PageRequest.of(page, size),
            )

        return SaoCatalogItemPage(
            items = result.content.map { entity -> entity.toModel() },
            page = result.number,
            size = result.size,
            totalItems = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    private fun SaoCatalogItemEntity.toModel(): SaoCatalogItem =
        SaoCatalogItem(
            gameKey = gameKey,
            defaultName = defaultName,
            type = type,
            rarity = rarity,
            styleColor = styleColor,
            locationIds = locationIds.toSet(),
            sourceHash = sourceHash,
            updatedAt = updatedAt,
        )
}
