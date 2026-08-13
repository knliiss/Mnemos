package com.mnemos.moderation.application.sanction.usecase.active

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.sanction.model.SanctionId
import java.time.Instant

data class ActiveBanView(
    val sanctionId: SanctionId,
    val userId: UserId,
    val occurredAt: Instant,
)
