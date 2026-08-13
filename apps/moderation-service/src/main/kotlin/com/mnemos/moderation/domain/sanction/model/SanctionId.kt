package com.mnemos.moderation.domain.sanction.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class SanctionId(
    val id: UUID,
) {
    companion object {
        fun generate(): SanctionId = SanctionId(UuidCreator.getTimeOrderedEpoch())
    }
}
