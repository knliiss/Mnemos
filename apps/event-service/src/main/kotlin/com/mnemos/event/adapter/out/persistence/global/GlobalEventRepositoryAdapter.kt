package com.mnemos.event.adapter.out.persistence.global

import com.mnemos.event.adapter.out.persistence.global.GlobalEventPersistenceMapper.toDomain
import com.mnemos.event.adapter.out.persistence.global.GlobalEventPersistenceMapper.toEntity
import com.mnemos.event.domain.global.model.GlobalEvent
import com.mnemos.event.domain.global.repository.GlobalEventRepository
import org.springframework.stereotype.Repository

@Repository
class GlobalEventRepositoryAdapter(
    private val eventJpaRepository: GlobalEventJpaRepository,
) : GlobalEventRepository {
    override fun save(globalEvent: GlobalEvent): GlobalEvent = eventJpaRepository.save(globalEvent.toEntity()).toDomain()
}
