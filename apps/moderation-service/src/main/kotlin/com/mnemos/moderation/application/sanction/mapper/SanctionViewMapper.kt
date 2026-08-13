package com.mnemos.moderation.application.sanction.mapper

import com.mnemos.moderation.application.sanction.model.SanctionView
import com.mnemos.moderation.domain.sanction.model.Sanction

object SanctionViewMapper {
    fun Sanction.toView(): SanctionView =
        SanctionView(
            id = id,
            userId = userId,
            type = type,
            reason = reason,
            internalNote = internalNote,
            createdByUserId = createdByUserId,
            createdAt = createdAt,
            expiresAt = expiresAt,
            status = status,
        )
}
