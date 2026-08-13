package com.mnemos.moderation.application.sanction.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import com.mnemos.moderation.domain.sanction.model.SanctionId
import java.time.Instant

data class SanctionView(
    val id: SanctionId,
    val userId: UserId,
    val type: DomainSanctionType,
    val reason: String,
    val internalNote: String?,
    val createdByUserId: UserId,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val status: DomainSanctionStatus,
)
