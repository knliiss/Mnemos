package com.mnemos.realtime.adapter.out.persistence.collector

import com.mnemos.realtime.domain.collector.model.CollectorActivation
import com.mnemos.realtime.domain.collector.model.CollectorActivationId
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

object CollectorActivationPersistenceMapper {
    fun CollectorActivation.toEntity(): CollectorActivationEntity =
        CollectorActivationEntity(
            id = id.id,
            userId = userId,
            tokenHash = tokenHash,
            createdAt = createdAt,
            expiresAt = expiresAt,
            credentialId = credentialId?.id,
            redeemedAt = redeemedAt,
        )

    fun CollectorActivationEntity.toDomain(): CollectorActivation =
        CollectorActivation.restore(
            id = CollectorActivationId(id),
            userId = userId,
            tokenHash = tokenHash,
            createdAt = createdAt,
            expiresAt = expiresAt,
            credentialId = credentialId?.let(::CollectorCredentialId),
            redeemedAt = redeemedAt,
        )
}
