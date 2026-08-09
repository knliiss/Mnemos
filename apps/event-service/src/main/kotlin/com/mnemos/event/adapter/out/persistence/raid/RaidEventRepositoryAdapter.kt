package com.mnemos.event.adapter.out.persistence.raid

import com.mnemos.event.adapter.out.persistence.raid.RaidEventPersistenceMapper.toDomain
import com.mnemos.event.adapter.out.persistence.raid.RaidEventPersistenceMapper.toEntity
import com.mnemos.event.domain.raid.model.RaidEvent
import com.mnemos.event.domain.raid.repository.RaidEventRepository
import org.springframework.stereotype.Repository

@Repository
class RaidEventRepositoryAdapter(
    private val jpaRepository: RaidEventJpaRepository,
) : RaidEventRepository {
    override fun save(event: RaidEvent): RaidEvent = jpaRepository.save(event.toEntity()).toDomain()
}
