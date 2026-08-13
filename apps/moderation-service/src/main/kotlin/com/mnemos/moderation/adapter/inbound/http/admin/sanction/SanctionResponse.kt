package com.mnemos.moderation.adapter.inbound.http.admin.sanction

import com.mnemos.moderation.contracts.shared.SanctionStatus
import com.mnemos.moderation.contracts.shared.SanctionType
import java.time.Instant
import java.util.UUID

data class SanctionResponse(
    val id: UUID,
    val userId: UUID,
    val type: SanctionType,
    val reason: String,
    val internalNote: String?,
    val createdByUserId: UUID,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val status: SanctionStatus,
)
