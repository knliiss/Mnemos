package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.domain.profile.model.PendingSocialSummary

object PendingSocialSummaryPersistenceMapper {
    fun PendingSocialSummary.toEntity(): PendingSocialSummaryEntity =
        PendingSocialSummaryEntity(
            accountId = accountId,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            revision = revision,
            occurredAt = occurredAt,
        )

    fun PendingSocialSummaryEntity.toDomain(): PendingSocialSummary =
        PendingSocialSummary(
            accountId = accountId,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            revision = revision,
            occurredAt = occurredAt,
        )
}
