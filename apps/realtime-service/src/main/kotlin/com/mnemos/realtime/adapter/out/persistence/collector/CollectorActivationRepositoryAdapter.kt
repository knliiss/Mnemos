package com.mnemos.realtime.adapter.out.persistence.collector

import com.mnemos.realtime.adapter.out.persistence.collector.CollectorActivationPersistenceMapper.toDomain
import com.mnemos.realtime.adapter.out.persistence.collector.CollectorActivationPersistenceMapper.toEntity
import com.mnemos.realtime.domain.collector.model.CollectorActivation
import com.mnemos.realtime.domain.collector.repository.CollectorActivationRepository
import org.springframework.stereotype.Repository

@Repository
class CollectorActivationRepositoryAdapter(
    private val jpaRepository: CollectorActivationJpaRepository,
) : CollectorActivationRepository {
    override fun create(activation: CollectorActivation): CollectorActivation =
        jpaRepository
            .save(activation.toEntity())
            .toDomain()

    override fun save(activation: CollectorActivation): CollectorActivation =
        jpaRepository
            .save(activation.toEntity())
            .toDomain()

    override fun findByTokenHashForUpdate(tokenHash: String): CollectorActivation? =
        jpaRepository
            .findByTokenHashForUpdate(tokenHash)
            ?.toDomain()
}
