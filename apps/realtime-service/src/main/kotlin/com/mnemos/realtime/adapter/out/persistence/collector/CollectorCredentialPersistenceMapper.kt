package com.mnemos.realtime.adapter.out.persistence.collector

import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

object CollectorCredentialPersistenceMapper {
    fun CollectorCredential.toEntity(): CollectorCredentialEntity =
        CollectorCredentialEntity(
            id = id.id,
            name = name,
            secretHash = secretHash,
            enabled = enabled,
            createdAt = createdAt,
            updatedAt = updatedAt,
            revokedAt = revokedAt,
        )

    fun CollectorCredentialEntity.toDomain(): CollectorCredential =
        CollectorCredential.restore(
            id = CollectorCredentialId(id),
            name = name,
            secretHash = secretHash,
            enabled = enabled,
            createdAt = createdAt,
            updatedAt = updatedAt,
            revokedAt = revokedAt,
        )
}
