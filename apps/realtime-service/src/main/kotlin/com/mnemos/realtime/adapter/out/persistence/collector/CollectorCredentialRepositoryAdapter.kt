package com.mnemos.realtime.adapter.out.persistence.collector

import com.mnemos.realtime.adapter.out.persistence.collector.CollectorCredentialPersistenceMapper.toDomain
import com.mnemos.realtime.adapter.out.persistence.collector.CollectorCredentialPersistenceMapper.toEntity
import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Repository

@Repository
class CollectorCredentialRepositoryAdapter(
    private val jpaRepository: CollectorCredentialJpaRepository,
) : CollectorCredentialRepository {
    override fun create(credential: CollectorCredential): CollectorCredential =
        jpaRepository
            .save(credential.toEntity())
            .toDomain()

    override fun save(credential: CollectorCredential): CollectorCredential =
        jpaRepository
            .save(credential.toEntity())
            .toDomain()

    override fun findById(id: CollectorCredentialId): CollectorCredential? =
        jpaRepository
            .findById(id.id)
            .orElse(null)
            ?.toDomain()
}
