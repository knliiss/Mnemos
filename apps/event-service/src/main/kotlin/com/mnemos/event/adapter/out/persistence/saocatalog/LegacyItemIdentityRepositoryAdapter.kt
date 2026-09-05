package com.mnemos.event.adapter.out.persistence.saocatalog

import com.mnemos.event.adapter.out.persistence.item.ItemJpaRepository
import com.mnemos.event.application.saocatalog.port.out.LegacyItemIdentity
import com.mnemos.event.application.saocatalog.port.out.LegacyItemIdentityRepository
import com.mnemos.event.domain.item.model.ItemId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Repository
class LegacyItemIdentityRepositoryAdapter(
    private val itemRepository: ItemJpaRepository,
    private val identityRepository: ItemCanonicalIdentityJpaRepository,
    private val clock: Clock,
) : LegacyItemIdentityRepository {
    @Transactional(readOnly = true)
    override fun findAll(): List<LegacyItemIdentity> =
        itemRepository.findAll().map { item ->
            LegacyItemIdentity(
                itemId = ItemId(item.id),
                name = item.name,
                type = item.type,
                gameKey = item.gameKey,
            )
        }

    @Transactional
    override fun saveCanonicalIdentity(
        itemId: ItemId,
        gameKey: String,
    ) {
        val entity =
            identityRepository.findById(itemId.id).orElseGet {
                ItemCanonicalIdentityEntity(
                    itemId = itemId.id,
                    gameKey = gameKey,
                    resolvedAt = Instant.now(clock),
                )
            }

        entity.gameKey = gameKey
        entity.resolvedAt = Instant.now(clock)
        identityRepository.save(entity)
    }
}
