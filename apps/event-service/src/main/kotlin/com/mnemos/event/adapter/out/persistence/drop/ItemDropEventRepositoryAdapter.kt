package com.mnemos.event.adapter.out.persistence.drop

import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventPersistenceMapper.toDomain
import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventPersistenceMapper.toEntity
import com.mnemos.event.domain.drop.model.ItemDropEvent
import com.mnemos.event.domain.drop.repository.ItemDropEventRepository
import org.springframework.stereotype.Repository

@Repository
class ItemDropEventRepositoryAdapter(
    private val itemDropEventJpaRepository: ItemDropEventJpaRepository,
) : ItemDropEventRepository {
    override fun save(itemDropEvent: ItemDropEvent): ItemDropEvent = itemDropEventJpaRepository.save(itemDropEvent.toEntity()).toDomain()
}
