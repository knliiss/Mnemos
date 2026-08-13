package com.mnemos.moderation.contracts.event

import com.mnemos.moderation.contracts.shared.SanctionStatus
import com.mnemos.moderation.contracts.shared.SanctionType
import java.time.Instant
import java.util.UUID

data class SanctionStateV1(
    val sanctionId: UUID,
    val userId: UUID,
    val type: SanctionType,
    val status: SanctionStatus,
    val occurredAt: Instant,
)
