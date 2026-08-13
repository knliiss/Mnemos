package com.mnemos.moderation.adapter.out.persistence.sanction

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.sanction.model.Sanction
import com.mnemos.moderation.domain.sanction.model.SanctionId

object SanctionPersistenceMapper {
    fun Sanction.toEntity(): SanctionEntity =
        SanctionEntity(
            id = id.id,
            userId = userId.id,
            type = type,
            reason = reason,
            internalNote = internalNote,
            createdByUserId = createdByUserId.id,
            createdAt = createdAt,
            expiresAt = expiresAt,
            status = status,
        )

    fun SanctionEntity.toDomain(): Sanction =
        Sanction.restore(
            id = SanctionId(id),
            userId = UserId(userId),
            type = type,
            reason = reason,
            internalNote = internalNote,
            createdByUserId = UserId(createdByUserId),
            createdAt = createdAt,
            expiresAt = expiresAt,
            status = status,
        )

    fun SanctionEntity.updateFrom(sanction: Sanction) {
        status = sanction.status
    }
}
