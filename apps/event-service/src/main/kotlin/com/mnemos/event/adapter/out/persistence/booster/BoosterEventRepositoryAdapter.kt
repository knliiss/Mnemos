package com.mnemos.event.adapter.out.persistence.booster

import com.mnemos.event.adapter.out.persistence.booster.BoosterEventPersistenceMapper.toDomain
import com.mnemos.event.adapter.out.persistence.booster.BoosterEventPersistenceMapper.toEntity
import com.mnemos.event.domain.booster.model.BoosterEvent
import com.mnemos.event.domain.booster.repository.BoosterEventRepository
import org.springframework.stereotype.Repository

@Repository
class BoosterEventRepositoryAdapter(
    private val jpaRepository: BoosterEventJpaRepository,
) : BoosterEventRepository {
    override fun save(boosterEvent: BoosterEvent): BoosterEvent = jpaRepository.save(boosterEvent.toEntity()).toDomain()
}
