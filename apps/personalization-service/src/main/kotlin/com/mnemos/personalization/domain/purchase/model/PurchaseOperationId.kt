package com.mnemos.personalization.domain.purchase.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class PurchaseOperationId(val id: UUID) {
    companion object {
        fun generate(): PurchaseOperationId = PurchaseOperationId(UuidCreator.getTimeOrderedEpoch())
    }

    override fun toString(): String = id.toString()
}
