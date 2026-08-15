package com.mnemos.personalization.domain.revenue.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class CreatorRevenueOperationId(
    val id: UUID,
) {
    companion object {
        fun generate(): CreatorRevenueOperationId = CreatorRevenueOperationId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
